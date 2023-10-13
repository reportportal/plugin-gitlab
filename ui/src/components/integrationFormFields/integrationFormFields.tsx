import { IntegrationFormFieldsInterface } from 'moduleFederation/common';
import { FC, useEffect } from 'react';

import { LABELS } from '../constants';

const INTEGRATION_NAME = 'integrationName';
const URL = 'url';
const PROJECT = 'project';
const API_TOKEN = 'apiToken';

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
      [SECRET_FIELDS_KEY]: [API_TOKEN],
    });
  }, []);

  return (
    <>
      <FieldElement
        name={INTEGRATION_NAME}
        label={LABELS.INTEGRATION_NAME}
        validate={btsIntegrationName}
        disabled={disabled}
        isRequired
      >
        <FieldErrorHint provideHint={false}>
          <FieldText maxLength={55} defaultWidth={false} />
        </FieldErrorHint>
      </FieldElement>
      <FieldElement name={URL} label={LABELS.URL} validate={btsUrl} disabled={disabled} isRequired>
        <FieldErrorHint provideHint={false}>
          <FieldText defaultWidth={false} />
        </FieldErrorHint>
      </FieldElement>
      <FieldElement
        name={PROJECT}
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
        name={API_TOKEN}
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
