import React, { useState, useEffect } from "react";
import { taskService } from "./api/taskService";
import TaskList from "./components/TaskList";
import TaskForm from "./components/TaskForm";
import "./App.css";

export default function App() {
  const [tasks, setTasks] = useState([]);
  const [editingTask, setEditingTask] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [error, setError] = useState(null);

  const loadTasks = async () => {
    try {
      const data = await taskService.getAll();
      setTasks(data);
      setError(null);
    } catch (err) {
      setError("Impossible de charger les tâches. Le backend est-il démarré ?");
    }
  };

  useEffect(() => {
    loadTasks();
  }, []);

  const handleCreate = () => {
    setEditingTask(null);
    setShowForm(true);
  };

  const handleEdit = (task) => {
    setEditingTask(task);
    setShowForm(true);
  };

  const handleSubmit = async (taskData) => {
    if (editingTask) {
      await taskService.update(editingTask.id, taskData);
    } else {
      await taskService.create(taskData);
    }
    setShowForm(false);
    setEditingTask(null);
    loadTasks();
  };

  const handleDelete = async (id) => {
    await taskService.delete(id);
    loadTasks();
  };

  return (
    <div className="container">
      <h1>📋 Mes tâches</h1>

      {error && <p className="error">{error}</p>}

      {!showForm && (
        <button data-testid="new-task-button" onClick={handleCreate}>
          + Nouvelle tâche
        </button>
      )}

      {showForm && (
        <TaskForm
          initialTask={editingTask}
          onSubmit={handleSubmit}
          onCancel={() => {
            setShowForm(false);
            setEditingTask(null);
          }}
        />
      )}

      <TaskList tasks={tasks} onEdit={handleEdit} onDelete={handleDelete} />
    </div>
  );
}
