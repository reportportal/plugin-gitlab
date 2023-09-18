import { IntegrationFormFields, IntegrationSettings } from 'components';

window.RP.registerPlugin({
  name: 'Gitlab',
  extensions: [
    {
      name: 'integrationFormFields',
      title: 'Gitlab plugin fields',
      type: 'uiExtension:integrationFormFields',
      component: IntegrationFormFields,
    },
    {
      name: 'integrationSettings',
      title: 'Gitlab plugin settings',
      type: 'uiExtension:integrationSettings',
      component: IntegrationSettings,
    },
  ],
});
