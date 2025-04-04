/*
 * CloudBeaver - Cloud Database Manager
 * Copyright (C) 2020-2024 DBeaver Corp and others
 *
 * Licensed under the Apache License, Version 2.0.
 * you may not use this file except in compliance with the License.
 */
import { AdministrationScreenService } from '@cloudbeaver/core-administration';
import { importLazyComponent } from '@cloudbeaver/core-blocks';
import { Bootstrap, injectable } from '@cloudbeaver/core-di';
import { ServerConfigResource } from '@cloudbeaver/core-root';
import { ScreenService } from '@cloudbeaver/core-routing';

const AdministrationScreen = importLazyComponent(() => import('./AdministrationScreen.js').then(m => m.AdministrationScreen));
const ConfigurationWizardScreen = importLazyComponent(() =>
  import('./ConfigurationWizard/ConfigurationWizardScreen.js').then(m => m.ConfigurationWizardScreen),
);

@injectable()
export class AdministrationScreenServiceBootstrap extends Bootstrap {
  constructor(
    private readonly screenService: ScreenService,
    private readonly administrationScreenService: AdministrationScreenService,
    private readonly serverConfigResource: ServerConfigResource,
  ) {
    super();
  }

  override register(): void {
    const canActivate = () => this.administrationScreenService.handleCanActivate.bind(this.administrationScreenService);

    this.screenService.create({
      name: AdministrationScreenService.screenName,
      routes: [
        {
          name: AdministrationScreenService.screenName,
          path: '/admin',
          canActivate,
        },
        {
          name: AdministrationScreenService.itemRouteName,
          path: '/:item',
          canActivate,
        },
        {
          name: AdministrationScreenService.itemSubRouteName,
          path: '/:sub',
          canActivate,
        },
        {
          name: AdministrationScreenService.itemSubParamRouteName,
          path: '/:param',
          canActivate,
        },
      ],
      component: AdministrationScreen,
      onActivate: this.administrationScreenService.handleActivate.bind(this.administrationScreenService),
      onDeactivate: this.administrationScreenService.handleDeactivate.bind(this.administrationScreenService),
      canDeActivate: this.administrationScreenService.handleCanDeActivate.bind(this.administrationScreenService),
    });

    this.screenService.create({
      name: AdministrationScreenService.setupName,
      routes: [
        {
          name: AdministrationScreenService.setupName,
          path: '/setup',
          canActivate,
        },
        {
          name: AdministrationScreenService.setupItemRouteName,
          path: '/:item',
          canActivate,
        },
        {
          name: AdministrationScreenService.setupItemSubRouteName,
          path: '/:sub',
          canActivate,
        },
        {
          name: AdministrationScreenService.setupItemSubParamRouteName,
          path: '/:param',
          canActivate,
        },
      ],
      component: ConfigurationWizardScreen,
      onActivate: this.administrationScreenService.handleActivate.bind(this.administrationScreenService),
      onDeactivate: this.administrationScreenService.handleDeactivate.bind(this.administrationScreenService),
      canDeActivate: this.administrationScreenService.handleCanDeActivate.bind(this.administrationScreenService),
    });
  }

  override async load(): Promise<void> {
    await this.serverConfigResource.load();

    if (
      this.administrationScreenService.isConfigurationMode &&
      !this.screenService.isActive(this.screenService.routerService.route, AdministrationScreenService.setupName)
    ) {
      this.administrationScreenService.navigateToRoot();
    }
  }
}
