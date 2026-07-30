import { useState, useEffect } from "react";

export default function TaskForm({ initialTask, onSubmit, onCancel }) {
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [completed, setCompleted] = useState(false);

  useEffect(() => {
    if (initialTask) {
      setTitle(initialTask.title || "");
      setDescription(initialTask.description || "");
      setCompleted(initialTask.completed || false);
    }
  }, [initialTask]);

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit({ title, description, completed });
  };

  return (
    <form onSubmit={handleSubmit} data-testid="task-form">
      <label htmlFor="title">Titre</label>
      <input
        id="title"
        data-testid="task-title-input"
        type="text"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        required
      />

      <label htmlFor="description">Description</label>
      <textarea
        id="description"
        data-testid="task-description-input"
        value={description}
        onChange={(e) => setDescription(e.target.value)}
      />

      <label>
        <input
          type="checkbox"
          data-testid="task-completed-checkbox"
          checked={completed}
          onChange={(e) => setCompleted(e.target.checked)}
        />
        Terminée
      </label>

      <div>
        <button type="submit" data-testid="task-submit-button">
          Enregistrer
        </button>
        <button type="button" onClick={onCancel}>
          Annuler
        </button>
      </div>
    </form>
  );
}
