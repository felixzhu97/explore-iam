import { cva, type VariantProps } from 'class-variance-authority';

import { mergeClasses } from '../../utils/merge-classes';

export const buttonVariants = cva(
  mergeClasses(
    'focus-visible:border-ring focus-visible:ring-ring/50 aria-invalid:ring-destructive/20',
    'aria-invalid:border-destructive rounded-lg border border-transparent bg-clip-padding',
    'text-sm font-medium focus-visible:ring-3 aria-invalid:ring-3 inline-flex items-center',
    'justify-center whitespace-nowrap transition-all disabled:pointer-events-none disabled:border-border',
    'disabled:bg-surface disabled:text-muted-foreground disabled:opacity-100',
    'shrink-0 outline-none select-none',
  ),
  {
    variants: {
      zType: {
        default: 'border-border bg-surface text-foreground hover:border-foreground/25 hover:bg-surface',
        destructive:
          'border-transparent bg-destructive/10 text-destructive hover:bg-destructive/20 focus-visible:border-destructive/40 focus-visible:ring-destructive/20',
        outline: 'border-border bg-surface text-foreground hover:border-foreground/25 hover:bg-surface',
        secondary: 'border-border bg-surface text-foreground hover:border-foreground/25 hover:bg-surface',
        ghost: 'border border-transparent bg-transparent text-foreground hover:border-border hover:bg-surface',
        link: 'text-foreground underline-offset-4 hover:underline',
        primary: 'border-transparent bg-primary text-primary-foreground hover:bg-primary/90',
      },
      zSize: {
        default: 'h-8 gap-1.5 px-2.5',
        xs: 'h-6 gap-1 rounded-[min(var(--radius-md),10px)] px-2 text-xs',
        sm: 'h-7 gap-1 rounded-[min(var(--radius-md),12px)] px-2.5 text-[0.8rem]',
        lg: 'h-9 gap-1.5 px-3',
        icon: 'size-8',
      },
      zShape: {
        default: 'rounded-md',
        circle: 'rounded-full',
        square: 'rounded-none',
      },
      zFull: {
        true: 'w-full',
      },
      zLoading: {
        true: 'pointer-events-none opacity-50',
      },
      zDisabled: {
        true: 'pointer-events-none border-border bg-surface text-muted-foreground opacity-100',
      },
    },
    defaultVariants: {
      zType: 'default',
      zSize: 'default',
      zShape: 'default',
    },
  },
);

export type ZardButtonShapeVariants = NonNullable<VariantProps<typeof buttonVariants>['zShape']>;
export type ZardButtonSizeVariants = NonNullable<VariantProps<typeof buttonVariants>['zSize']>;
export type ZardButtonTypeVariants = NonNullable<VariantProps<typeof buttonVariants>['zType']>;
