'use client';

import { useRouter } from 'next/navigation';
import { CampaignListContainer } from '@/features/kiosk-campaign-list';
import { useAuth } from '@/shared/lib/auth-provider';
import { Campaign } from '@/shared/types';

export default function CampaignsPage() {
  const router = useRouter();
  const { currentKioskSession, handleLogout, refreshCurrentKioskSession } = useAuth();

  // Predefined amount click - check giftAidEnabled before routing
  const handleSelectCampaign = (campaign: Campaign, amount?: number) => {
    if (amount) {
      // Check if Gift Aid is enabled for this campaign
      if (campaign.configuration.giftAidEnabled) {
        // Predefined amount selected - go to gift aid with selected amount
        router.push(`/campaign/${campaign.id}?amount=${amount}&giftaid=true`);
      } else {
        // Gift Aid disabled - go directly to payment
        const amountPence = Math.round(amount * 100);
        const donation = {
          campaignId: campaign.id,
          amount: amountPence,
          organizationId: campaign.organizationId,
          isGiftAid: false,
          giftAidAccepted: false, // Explicitly set to false when disabled
          isRecurring: false,
          kioskId: currentKioskSession?.kioskId,
          donorName: '',
        };
        sessionStorage.setItem('donation', JSON.stringify(donation));
        sessionStorage.setItem('paymentBackPath', '/campaigns');
        router.push(`/payment/${campaign.id}`);
      }
    } else {
      // No amount (donate button) - go to details page
      router.push(`/campaign/${campaign.id}`);
    }
  };

  // View campaign details (card click or donate button)
  const handleViewDetails = (campaign: Campaign) => {
    router.push(`/campaign/${campaign.id}`);
  };

  return (
    <CampaignListContainer
      kioskSession={currentKioskSession}
      onSelectCampaign={handleSelectCampaign}
      onViewDetails={handleViewDetails}
      onLogout={handleLogout}
      refreshCurrentKioskSession={refreshCurrentKioskSession}
    />
  );
}
