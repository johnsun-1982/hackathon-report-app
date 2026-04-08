// Basic Angular Component Template
// Usage: Replace placeholders with your specific component requirements

import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { {{ServiceName}} } from '../services/{{serviceName}}.service';

@Component({
  selector: 'app-{{ComponentName}}',
  templateUrl: './{{componentName}}.component.html',
  styleUrls: ['./{{componentName}}.component.css']
})
export class {{ComponentName}}Component implements OnInit {
  
  {{dataProperty}}: any[] = [];
  {{formProperty}}: FormGroup;
  loading = false;
  error: string | null = null;
  
  constructor(
    private fb: FormBuilder,
    private {{serviceNameCamel}}: {{ServiceName}}
  ) {}
  
  ngOnInit(): void {
    this.initializeForm();
    this.loadData();
  }
  
  private initializeForm(): void {
    this.{{formProperty}} = this.fb.group({
      {{formFields}}
    });
  }
  
  loadData(): void {
    this.loading = true;
    this.error = null;
    
    this.{{serviceNameCamel}}.getAll().subscribe({
      next: (data) => {
        this.{{dataProperty}} = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load data. Please try again.';
        this.loading = false;
        console.error('Load error:', err);
      }
    });
  }
  
  onSubmit(): void {
    if (this.{{formProperty}}.valid) {
      this.loading = true;
      this.error = null;
      
      this.{{serviceNameCamel}}.create(this.{{formProperty}}.value).subscribe({
        next: () => {
          this.loadData();
          this.{{formProperty}}.reset();
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Failed to save data. Please try again.';
          this.loading = false;
          console.error('Save error:', err);
        }
      });
    } else {
      this.markFormGroupTouched(this.{{formProperty}});
    }
  }
  
  editItem(item: any): void {
    this.{{formProperty}}.patchValue(item);
  }
  
  deleteItem(item: any): void {
    if (confirm('Are you sure you want to delete this item?')) {
      this.loading = true;
      
      this.{{serviceNameCamel}}.delete(item.id).subscribe({
        next: () => {
          this.loadData();
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Failed to delete item. Please try again.';
          this.loading = false;
          console.error('Delete error:', err);
        }
      });
    }
  }
  
  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.values(formGroup.controls).forEach(control => {
      control.markAsTouched();
      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      }
    });
  }
  
  // Helper methods
  getErrorMessage(field: string): string {
    const control = this.{{formProperty}}.get(field);
    if (control?.errors) {
      if (control.errors['required']) {
        return 'This field is required';
      }
      if (control.errors['email']) {
        return 'Please enter a valid email address';
      }
      if (control.errors['minlength']) {
        return `Minimum length is ${control.errors['minlength'].requiredLength} characters`;
      }
      if (control.errors['pattern']) {
        return 'Invalid format';
      }
    }
    return '';
  }
  
  isFieldInvalid(field: string): boolean {
    const control = this.{{formProperty}}.get(field);
    return control ? control.invalid && (control.dirty || control.touched) : false;
  }
}
