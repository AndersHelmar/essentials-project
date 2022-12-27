package dk.cloudcreate.essentials.reactive.command;

import dk.cloudcreate.essentials.reactive.command.interceptor.*;
import dk.cloudcreate.essentials.shared.Exceptions;
import org.slf4j.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static dk.cloudcreate.essentials.shared.FailFast.requireNonNull;
import static dk.cloudcreate.essentials.shared.MessageFormatter.msg;

/**
 * Base implementation of the {@link CommandBus} - provides default implementation for all
 * operations except for {@link #sendAndDontWait(Object)}/{@link #sendAndDontWait(Object, Duration)}
 */
public abstract class AbstractCommandBus implements CommandBus {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    protected final List<CommandBusInterceptor> interceptors    = new ArrayList<>();
    protected final Set<CommandHandler>         commandHandlers = new HashSet<>();
    protected final SendAndDontWaitErrorHandler sendAndDontWaitErrorHandler;

    protected AbstractCommandBus(List<CommandBusInterceptor> interceptors) {
        this(new FallbackSendAndDontWaitErrorHandler(),
             interceptors);
    }

    protected AbstractCommandBus(SendAndDontWaitErrorHandler sendAndDontWaitErrorHandler,
                                 List<CommandBusInterceptor> interceptors) {
        requireNonNull(interceptors, "No interceptors list provided");
        this.sendAndDontWaitErrorHandler = requireNonNull(sendAndDontWaitErrorHandler, "No sendAndDontWaitErrorHandler provided");
        interceptors.forEach(this::addInterceptor);
    }

    /**
     * Key: Concrete Command type<br>
     * Value: {@link CommandHandler} that can handle the command type
     */
    protected final ConcurrentMap<Class<?>, CommandHandler> commandTypeToCommandHandlerCache = new ConcurrentHashMap<>();

    @Override
    public List<CommandBusInterceptor> getInterceptors() {
        return Collections.unmodifiableList(interceptors);
    }

    @Override
    public CommandBus addInterceptor(CommandBusInterceptor interceptor) {
        if (!interceptors.contains(interceptor)) {
            log.info("Adding CommandBusInterceptor: {}", interceptor);
            interceptors.add(requireNonNull(interceptor, "No interceptor provided"));
        }
        return this;
    }

    @Override
    public boolean hasInterceptor(CommandBusInterceptor interceptor) {
        return interceptors.contains(requireNonNull(interceptor, "No interceptor provided"));
    }

    @Override
    public CommandBus removeInterceptor(CommandBusInterceptor interceptor) {
        log.info("Removing CommandBusInterceptor: {}", interceptor);
        interceptors.remove(requireNonNull(interceptor, "No interceptor provided"));
        return this;
    }

    @Override
    public CommandBus addCommandHandler(CommandHandler commandHandler) {
        if (!hasCommandHandler(commandHandler)) {
            log.info("Adding CommandHandler: {}", commandHandler);
            if (commandHandlers.add(requireNonNull(commandHandler, "No commandHandler provided"))) {
                commandTypeToCommandHandlerCache.clear();
            }
        }
        return this;
    }

    @Override
    public CommandBus removeCommandHandler(CommandHandler commandHandler) {
        log.info("Removing CommandHandler: {}", commandHandler);
        if (commandHandlers.remove(requireNonNull(commandHandler, "No commandHandler provided"))) {
            commandTypeToCommandHandlerCache.clear();
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, C> R send(C command) {
        var commandHandler = findCommandHandlerCapableOfHandling(command);
        log.debug("Synchronously sending command of type '{}' to {} '{}'", command.getClass().getName(), CommandHandler.class.getSimpleName(), commandHandler.toString());
        return (R) CommandBusInterceptorChain.newInterceptorChain(command,
                                                                  commandHandler,
                                                                  interceptors,
                                                                  (interceptor, commandBusInterceptorChain) -> interceptor.interceptSend(command, commandBusInterceptorChain),
                                                                  commandHandler::handle)
                                             .proceed();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, C> Mono<R> sendAsync(C command) {
        var commandHandler = findCommandHandlerCapableOfHandling(command);
        log.debug("Asynchronously sending command of type '{}' to {} '{}'", command.getClass().getName(), CommandHandler.class.getSimpleName(), commandHandler.toString());
        return Mono.fromCallable(() -> (R) CommandBusInterceptorChain.newInterceptorChain(command,
                                                                                          commandHandler,
                                                                                          interceptors,
                                                                                          (interceptor, commandBusInterceptorChain) -> interceptor.interceptSendAsync(command, commandBusInterceptorChain),
                                                                                          commandHandler::handle)
                                                                     .proceed()).publishOn(Schedulers.boundedElastic());
    }

    @Override
    public CommandHandler findCommandHandlerCapableOfHandling(Object command) {
        requireNonNull(command, "No command provided");
        return commandTypeToCommandHandlerCache.computeIfAbsent(command.getClass(), commandType -> {
            var commandHandlersThatCanHandleCommand = commandHandlers.stream().filter(commandHandler -> commandHandler.canHandle(commandType)).collect(Collectors.toList());
            if (commandHandlersThatCanHandleCommand.isEmpty()) {
                throw new NoCommandHandlerFoundException(commandType, msg("Couldn't find a {} that can handle a command of type '{}'", CommandHandler.class.getSimpleName(), commandType.getName()));
            } else if (commandHandlersThatCanHandleCommand.size() > 1) {
                throw new MultipleCommandHandlersFoundException(commandType,
                                                                msg("There should only be one {} that can handle a given command. Found {} {}'s that all can handle a command of type '{}': {}",
                                                                    CommandHandler.class.getSimpleName(),
                                                                    commandHandlersThatCanHandleCommand.size(),
                                                                    CommandHandler.class.getSimpleName(),
                                                                    commandType.getName(),
                                                                    commandHandlersThatCanHandleCommand.stream().map(Object::toString).collect(Collectors.toList())));

            } else {
                return commandHandlersThatCanHandleCommand.get(0);
            }
        });
    }

    @Override
    public boolean hasCommandHandler(CommandHandler commandHandler) {
        return commandHandlers.contains(requireNonNull(commandHandler, "No commandHandler provided"));
    }

    /**
     * Fallback {@link SendAndDontWaitErrorHandler} that only error logs any issues.<br>
     * Note: If the {@link FallbackSendAndDontWaitErrorHandler} is used with a Durable Command Bus (e.g. using DurableQueues),
     * then any failing command will not be retried.<br>
     * Instead use {@link RethrowingSendAndDontWaitErrorHandler}
     */
    public static class FallbackSendAndDontWaitErrorHandler implements SendAndDontWaitErrorHandler {
        private static final Logger log = LoggerFactory.getLogger(FallbackSendAndDontWaitErrorHandler.class);

        @Override
        public void handleError(Exception exception, Object command, CommandHandler commandHandler) {
            log.error(msg("SendAndDontWait ERROR: {} '{}' failed to handle command: {}",
                          CommandHandler.class.getSimpleName(),
                          commandHandler.getClass().getName(),
                          command), exception);
        }
    }

    /**
     * Fallback {@link SendAndDontWaitErrorHandler} that error logs any issues and rethrows the exception.<br>
     * The {@link RethrowingSendAndDontWaitErrorHandler} is compatible with a Durable Command Bus (e.g. using DurableQueues),
     * as rethrowing the exceptions allows the command to be retried
     */
    public static class RethrowingSendAndDontWaitErrorHandler implements SendAndDontWaitErrorHandler {
        private static final Logger log = LoggerFactory.getLogger(FallbackSendAndDontWaitErrorHandler.class);

        @Override
        public void handleError(Exception exception, Object command, CommandHandler commandHandler) {
            log.error(msg("SendAndDontWait ERROR: {} '{}' failed to handle command: {}",
                          CommandHandler.class.getSimpleName(),
                          commandHandler.getClass().getName(),
                          command), exception);
            Exceptions.sneakyThrow(exception);
        }
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "{" +
                "interceptors=" + interceptors +
                ", commandHandlers=" + commandHandlers +
                '}';
    }
}
