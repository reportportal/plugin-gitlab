import { IntegrationFormFieldsInterface } from 'moduleFederation/common';
import { FC, useEffect } from 'react';

import { LABELS } from '../constants';

export const IntegrationFormFields: FC<IntegrationFormFieldsInterface> = (props) => {
  const { initialize, disabled, lineAlign, initialData, updateMetaData, ...extensionProps } = props;
  const {
    components: { FieldErrorHint, FieldElement, FieldText, FieldTextFlex },
    validators: { requiredField, btsUrl, btsProjectId, btsIntegrationName },
    constants: { SECRET_FIELDS_KEY },
  } = extensionProps;

  useEffect(() => {
    initialize(initialData);
    updateMetaData({
      [SECRET_FIELDS_KEY]: ['apiToken'],
    });
  }, []);

  return (
    <>
      <FieldElement
        name="integrationName"
        label={LABELS.INTEGRATION_NAME}
        validate={btsIntegrationName}
        disabled={disabled}
        isRequired
      >
        <FieldErrorHint provideHint={false}>
          <FieldText maxLength={55} defaultWidth={false} />
        </FieldErrorHint>
      </FieldElement>
      <FieldElement name="url" label={LABELS.URL} validate={btsUrl} disabled={disabled} isRequired>
        <FieldErrorHint provideHint={false}>
          <FieldText defaultWidth={false} />
        </FieldErrorHint>
      </FieldElement>
      <FieldElement
        name="project"
        label={LABELS.PROJECT}
        validate={btsProjectId}
        disabled={disabled}
        isRequired
      >
        <FieldErrorHint provideHint={false}>
          <FieldText maxLength={55} defaultWidth={false} />
        </FieldErrorHint>
      </FieldElement>
      <FieldElement
        name="apiToken"
        label={LABELS.TOKEN}
        disabled={disabled}
        validate={requiredField}
        isRequired
      >
        <FieldErrorHint provideHint={false}>
          <FieldTextFlex />
        </FieldErrorHint>
      </FieldElement>
    </>
  );
};
