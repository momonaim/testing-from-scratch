export default function TaskItem({ task, onEdit, onDelete }) {
  return (
    <tr className="task-item" data-testid="task-item">
      <td
        className={task.completed ? "completed" : ""}
        data-testid="task-title"
      >
        {task.title}
      </td>
      <td data-testid="task-description">{task.description}</td>
      <td data-testid="task-status">
        {task.completed ? "✅ Terminée" : "⏳ En cours"}
      </td>
      <td>
        <button data-testid="task-edit-button" onClick={() => onEdit(task)}>
          Modifier
        </button>
        <button
          data-testid="task-delete-button"
          onClick={() => {
            if (window.confirm("Supprimer cette tâche ?")) {
              onDelete(task.id);
            }
          }}
        >
          Supprimer
        </button>
      </td>
    </tr>
  );
}
