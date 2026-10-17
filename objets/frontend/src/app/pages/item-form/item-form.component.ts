import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Item } from '../../models/item.model';
import { ItemService } from '../../services/item.service';

@Component({
  selector: 'app-item-form',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './item-form.component.html',
  styleUrl: './item-form.component.scss'
})
export class ItemFormComponent implements OnInit {

  isEditMode = false;
  itemId: number | null = null;
  loading = false;
  saving = false;
  error = '';

  selectedFile: File | null = null;
  previewUrl: string | null = null;

  item: Partial<Item> = {
    title: '',
    description: '',
    category: '',
    location: '',
    date: new Date().toISOString().split('T')[0],
    type: 'LOST',
    status: 'ACTIVE',
    reporterName: '',
    reporterContact: '',
    photoUrl: ''
  };

  categories = [
    'Électronique', 'Vêtements', 'Documents', 'Bijoux & Accessoires',
    'Sacs & Bagages', 'Clés', 'Animaux', 'Sport & Loisirs', 'Livres & Papeterie', 'Autre'
  ];

  constructor(
    private itemService: ItemService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Pré-remplir le type depuis les queryParams (/items/new?type=LOST)
    this.route.queryParams.subscribe(params => {
      if (params['type']) {
        this.item.type = params['type'];
      }
    });

    // Mode édition si l'URL contient un ID
    const id = this.route.snapshot.paramMap.get('id');
    if (id && id !== 'new') {
      this.isEditMode = true;
      this.itemId = +id;
      this.loadItem(this.itemId);
    }
  }

  loadItem(id: number): void {
    this.loading = true;
    this.itemService.getById(id).subscribe({
      next: (item) => {
        this.item = { ...item };
        if (item.photoUrl) {
          this.previewUrl = item.photoUrl;
        }
        this.loading = false;
      },
      error: () => {
        this.error = 'Impossible de charger l\'objet.';
        this.loading = false;
      }
    });
  }

  onFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      this.selectedFile = input.files[0];
      const reader = new FileReader();
      reader.onload = (e) => {
        this.previewUrl = e.target?.result as string;
      };
      reader.readAsDataURL(this.selectedFile);
    }
  }

  removePhoto(): void {
    this.selectedFile = null;
    this.previewUrl = null;
    this.item.photoUrl = '';
  }

  onSubmit(): void {
    if (!this.validate()) return;

    this.saving = true;
    this.error = '';

    const save$ = this.selectedFile
      ? (this.isEditMode
          ? this.itemService.updateWithPhoto(this.itemId!, this.item, this.selectedFile)
          : this.itemService.createWithPhoto(this.item, this.selectedFile))
      : (this.isEditMode
          ? this.itemService.update(this.itemId!, this.item)
          : this.itemService.create(this.item));

    save$.subscribe({
      next: (saved) => {
        this.saving = false;
        this.router.navigate(['/items', saved.id]);
      },
      error: (err) => {
        this.saving = false;
        this.error = err?.error?.error || 'Erreur lors de la sauvegarde.';
      }
    });
  }

  validate(): boolean {
    if (!this.item.title?.trim()) { this.error = 'Le titre est obligatoire.'; return false; }
    if (!this.item.category?.trim()) { this.error = 'La catégorie est obligatoire.'; return false; }
    if (!this.item.location?.trim()) { this.error = 'La localisation est obligatoire.'; return false; }
    if (!this.item.date) { this.error = 'La date est obligatoire.'; return false; }
    if (!this.item.type) { this.error = 'Le type est obligatoire.'; return false; }
    return true;
  }

  get formTitle(): string {
    return this.isEditMode ? 'Modifier l\'objet' : 'Déclarer un objet';
  }
}
