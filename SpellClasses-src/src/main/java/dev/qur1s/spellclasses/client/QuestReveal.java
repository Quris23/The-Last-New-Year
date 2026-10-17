package dev.qur1s.spellclasses.client;

/** While active, quests of the shared "История" chapter show their real icon and title even if not completed yet. */
public final class QuestReveal {
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    /** Quests whose hidden/visible decision has already been written to the log (diagnostics). */
    public static final java.util.Set<Long> LOGGED = java.util.concurrent.ConcurrentHashMap.newKeySet();

    private QuestReveal() {
    }

    public static boolean active() {
        if (DEPTH.get() > 0) return true;
        // Called from FTB's own icon/title caching of a quest (it asks the quest's first task): the real value is needed there.
        return StackWalker.getInstance().walk(frames -> frames.limit(8).anyMatch(frame ->
                frame.getClassName().equals("dev.ftb.mods.ftbquests.quest.Quest")
                        && (frame.getMethodName().equals("getAltIcon") || frame.getMethodName().equals("getAltTitle"))));
    }

    public static void begin() {
        DEPTH.set(DEPTH.get() + 1);
    }

    public static void end() {
        DEPTH.set(Math.max(0, DEPTH.get() - 1));
    }

    public static void run(Runnable action) {
        begin();
        try {
            action.run();
        } finally {
            end();
        }
    }
}
