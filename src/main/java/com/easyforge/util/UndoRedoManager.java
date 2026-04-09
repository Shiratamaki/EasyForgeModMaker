package com.easyforge.util;

import java.util.Stack;

/**
 * 通用的撤销/重做管理器，使用命令模式存储 Runnable 动作
 */
public class UndoRedoManager {
    private final Stack<Runnable> undoStack = new Stack<>();
    private final Stack<Runnable> redoStack = new Stack<>();
    private boolean isExecuting = false;

    /**
     * 执行一个动作并记录其撤销动作
     * @param action     要执行的操作
     * @param undoAction 撤销该操作需要执行的动作
     */
    public void execute(Runnable action, Runnable undoAction) {
        if (isExecuting) return;
        isExecuting = true;
        try {
            action.run();
            undoStack.push(undoAction);
            redoStack.clear();
        } finally {
            isExecuting = false;
        }
    }

    /**
     * 撤销上一次操作
     */
    public void undo() {
        if (undoStack.isEmpty()) return;
        isExecuting = true;
        try {
            Runnable undoAction = undoStack.pop();
            undoAction.run();
            redoStack.push(undoAction);
        } finally {
            isExecuting = false;
        }
    }

    /**
     * 重做上一次撤销的操作
     */
    public void redo() {
        if (redoStack.isEmpty()) return;
        isExecuting = true;
        try {
            Runnable redoAction = redoStack.pop();
            redoAction.run();
            undoStack.push(redoAction);
        } finally {
            isExecuting = false;
        }
    }

    /**
     * 清空所有历史记录（切换项目时调用）
     */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }

    public boolean canUndo() { return !undoStack.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }
}