/*
 * CloudBeaver - Cloud Database Manager
 * Copyright (C) 2020-2024 DBeaver Corp and others
 *
 * Licensed under the Apache License, Version 2.0.
 * you may not use this file except in compliance with the License.
 */
import type { TLocalizationToken } from './TLocalizationToken.js';

export type TranslateFn = <T extends TLocalizationToken | undefined>(token: T, fallback?: T, args?: Record<string | number, any>) => T;
