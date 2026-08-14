import {
  ChangeDetectionStrategy,
  Component,
  computed,
  ElementRef,
  inject,
  input,
  ViewEncapsulation,
  booleanAttribute,
} from '@angular/core';

import type { ClassValue } from 'clsx';

import { mergeClasses } from '../../utils/merge-classes';

import {
  buttonVariants,
  type ZardButtonShapeVariants,
  type ZardButtonSizeVariants,
  type ZardButtonTypeVariants,
} from './button.variants';

@Component({
  selector: 'z-button, button[z-button], a[z-button]',
  template: `<ng-content />`,
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None,
  host: {
    '[class]': 'classes()',
    '[attr.data-disabled]': 'isHostElement() && zDisabled() || null',
    '[attr.aria-disabled]': 'isHostElement() && zDisabled() || null',
    '[attr.disabled]': 'isHostElement() && zDisabled() ? "" : null',
  },
  exportAs: 'zButton',
})
export class ZardButtonComponent {
  private readonly elementRef = inject(ElementRef<HTMLElement>);

  readonly zType = input<ZardButtonTypeVariants>('default');
  readonly zSize = input<ZardButtonSizeVariants>('default');
  readonly zShape = input<ZardButtonShapeVariants>('default');
  readonly class = input<ClassValue>('');
  readonly zFull = input(false, { transform: booleanAttribute });
  readonly zLoading = input(false, { transform: booleanAttribute });
  readonly zDisabled = input(false, { transform: booleanAttribute });

  protected readonly classes = computed(() =>
    mergeClasses(
      buttonVariants({
        zType: this.zType(),
        zSize: this.zSize(),
        zShape: this.zShape(),
        zFull: this.zFull(),
        zLoading: this.zLoading(),
        zDisabled: this.zDisabled(),
      }),
      this.class(),
    ),
  );

  protected readonly isHostElement = computed(() => {
    const tag = this.elementRef.nativeElement.tagName;
    return tag === 'BUTTON' || tag === 'A' || tag === 'Z-BUTTON';
  });
}
