/*
 * Generated by asn1c-0.9.24 (http://lionet.info/asn1c)
 * From ASN.1 module "InformationElements"
 * 	found in "../asn/InformationElements.asn"
 * 	`asn1c -fcompound-names -fnative-types`
 */

#ifndef	_SysInfoType15bis_va40ext_IEs_H_
#define	_SysInfoType15bis_va40ext_IEs_H_


#include <asn_application.h>

/* Including external dependencies */
#include "UE-Positioning-GANSS-ReferenceTime-va40ext.h"
#include <constr_SEQUENCE.h>

#ifdef __cplusplus
extern "C" {
#endif

/* SysInfoType15bis-va40ext-IEs */
typedef struct SysInfoType15bis_va40ext_IEs {
	UE_Positioning_GANSS_ReferenceTime_va40ext_t	 ue_positioning_GANSS_ReferenceTime;
	
	/* Context for parsing across buffer boundaries */
	asn_struct_ctx_t _asn_ctx;
} SysInfoType15bis_va40ext_IEs_t;

/* Implementation */
extern asn_TYPE_descriptor_t asn_DEF_SysInfoType15bis_va40ext_IEs;

#ifdef __cplusplus
}
#endif

#endif	/* _SysInfoType15bis_va40ext_IEs_H_ */
#include <asn_internal.h>