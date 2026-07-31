import { test, expect } from '@playwright/test';
import { randomUUID } from 'node:crypto';

test.describe('Gestion des tâches React', () => {
    test('création d\'une tâche', async ({ page }) => {
        const title = `Tâche Playwright ${randomUUID()}`;

        await page.goto('/');

        await page.getByTestId('new-task-button').click();
        await page.getByTestId('task-title-input').fill(title);
        await page.getByTestId('task-description-input').fill('Créée via un test E2E');
        await page.getByTestId('task-submit-button').click();

        await expect(page.getByTestId('task-title').filter({ hasText: title })).toHaveCount(1);
    });

    test('modification d\'une tâche', async ({ page }) => {
        const originalTitle = `À modifier ${randomUUID()}`;
        const updatedTitle = `Titre modifié ${randomUUID()}`;

        await page.goto('/');

        await page.getByTestId('new-task-button').click();
        await page.getByTestId('task-title-input').fill(originalTitle);
        await page.getByTestId('task-submit-button').click();

        await page.getByTestId('task-title').filter({ hasText: originalTitle }).first().locator('..').getByTestId('task-edit-button').click();
        await page.getByTestId('task-title-input').fill(updatedTitle);
        await page.getByTestId('task-submit-button').click();

        await expect(page.getByTestId('task-title').filter({ hasText: updatedTitle })).toHaveCount(1);
    });

    test('suppression d\'une tâche', async ({ page }) => {
        const title = `À supprimer ${randomUUID()}`;

        await page.goto('/');

        await page.getByTestId('new-task-button').click();
        await page.getByTestId('task-title-input').fill(title);
        await page.getByTestId('task-submit-button').click();

        page.once('dialog', dialog => dialog.accept());
        await page.getByTestId('task-title').filter({ hasText: title }).first().locator('..').getByTestId('task-delete-button').click();

        await expect(page.getByTestId('task-title').filter({ hasText: title })).toHaveCount(0);
    });

    test('marquer une tâche comme terminée', async ({ page }) => {
        const title = `À terminer ${randomUUID()}`;

        await page.goto('/');

        await page.getByTestId('new-task-button').click();
        await page.getByTestId('task-title-input').fill(title);
        await page.getByTestId('task-completed-checkbox').check();
        await page.getByTestId('task-submit-button').click();

        await expect(page.getByTestId('task-status').filter({ hasText: '✅ Terminée' })).toHaveCount(1);
    });
});
