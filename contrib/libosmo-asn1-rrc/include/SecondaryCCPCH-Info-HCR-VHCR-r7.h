/*
 * Generated by asn1c-0.9.24 (http://lionet.info/asn1c)
 * From ASN.1 module "InformationElements"
 * 	found in "../asn/InformationElements.asn"
 * 	`asn1c -fcompound-names -fnative-types`
 */

#ifndef	_SecondaryCCPCH_Info_HCR_VHCR_r7_H_
#define	_SecondaryCCPCH_Info_HCR_VHCR_r7_H_


#include <asn_application.h>

/* Including external dependencies */
#include "IndividualTimeslotInfo-r7.h"
#include "SCCPCH-ChannelisationCodeList.h"
#include <constr_SEQUENCE.h>
#include "IndividualTimeslotInfo-VHCR.h"
#include "SCCPCH-ChannelisationCodeList-VHCR.h"
#include <constr_CHOICE.h>

#ifdef __cplusplus
extern "C" {
#endif

/* Dependencies */
typedef enum SecondaryCCPCH_Info_HCR_VHCR_r7__modeSpecificInfo_PR {
	SecondaryCCPCH_Info_HCR_VHCR_r7__modeSpecificInfo_PR_NOTHING,	/* No components present */
	SecondaryCCPCH_Info_HCR_VHCR_r7__modeSpecificInfo_PR_tdd384,
	SecondaryCCPCH_Info_HCR_VHCR_r7__modeSpecificInfo_PR_tdd768
} SecondaryCCPCH_Info_HCR_VHCR_r7__modeSpecificInfo_PR;

/* SecondaryCCPCH-Info-HCR-VHCR-r7 */
typedef struct SecondaryCCPCH_Info_HCR_VHCR_r7 {
	struct SecondaryCCPCH_Info_HCR_VHCR_r7__modeSpecificInfo {
		SecondaryCCPCH_Info_HCR_VHCR_r7__modeSpecificInfo_PR present;
		union SecondaryCCPCH_Info_HCR_VHCR_r7__modeSpecificInfo_u {
			struct SecondaryCCPCH_Info_HCR_VHCR_r7__modeSpecificInfo__tdd384 {
				IndividualTimeslotInfo_r7_t	 individualTimeslotInfo;
				SCCPCH_ChannelisationCodeList_t	 channelisationCode;
				
				/* Context for parsing across buffer boundaries */
				asn_struct_ctx_t _asn_ctx;
			} tdd384;
			struct SecondaryCCPCH_Info_HCR_VHCR_r7__modeSpecificInfo__tdd768 {
				IndividualTimeslotInfo_VHCR_t	 individualTimeslotInfo;
				SCCPCH_ChannelisationCodeList_VHCR_t	 channelisationCode;
				
				/* Context for parsing across buffer boundaries */
				asn_struct_ctx_t _asn_ctx;
			} tdd768;
		} choice;
		
		/* Context for parsing across buffer boundaries */
		asn_struct_ctx_t _asn_ctx;
	} modeSpecificInfo;
	
	/* Context for parsing across buffer boundaries */
	asn_struct_ctx_t _asn_ctx;
} SecondaryCCPCH_Info_HCR_VHCR_r7_t;

/* Implementation */
extern asn_TYPE_descriptor_t asn_DEF_SecondaryCCPCH_Info_HCR_VHCR_r7;

#ifdef __cplusplus
}
#endif

#endif	/* _SecondaryCCPCH_Info_HCR_VHCR_r7_H_ */
#include <asn_internal.h>
