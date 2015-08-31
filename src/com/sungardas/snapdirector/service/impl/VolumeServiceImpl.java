package com.sungardas.snapdirector.service.impl;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.sungardas.snapdirector.aws.dynamodb.model.BackupEntry;
import com.sungardas.snapdirector.aws.dynamodb.repository.BackupRepository;
import com.sungardas.snapdirector.dto.VolumeDto;
import com.sungardas.snapdirector.dto.converter.VolumeDtoConverter;
import com.sungardas.snapdirector.exception.DataAccessException;
import com.sungardas.snapdirector.service.VolumeService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class VolumeServiceImpl implements VolumeService {

    public static final String REMOVED_VOLUME_STATE = "removed";

    private static final Logger LOG = LogManager.getLogger(VolumeServiceImpl.class);

    private static final String EMPTY = "";

    @Autowired
    private AmazonEC2 amazonEC2;

    @Autowired
    private BackupRepository backupRepository;

    @Value("${amazon.aws.region}")
    private String region;

    @Override
    public Set<VolumeDto> getVolumes() {
        LOG.debug("Getting volume list ...");
        amazonEC2.setRegion(Region.getRegion(Regions.fromName(region)));
        return getVolumes(amazonEC2);
    }

    @Override
    public Set<VolumeDto> getVolumesByRegion(Region region) {
        LOG.debug("Getting volume list for region [{}]", region);
        amazonEC2.setRegion(region);
        Set<VolumeDto> volumes = getVolumes(amazonEC2);
        return volumes;
    }

    @Override
    public boolean isExists(String volumeId) {
        for (VolumeDto dto : getVolumes()) {
            if (dto.getVolumeId().equals(volumeId)) {
                return true;
            }
        }
        return false;
    }

    private Set<VolumeDto> getVolumes(AmazonEC2 amazonEC2) {
        try {
            Set<VolumeDto> result = new TreeSet<>(volumeDtoComparator);
            result.addAll(VolumeDtoConverter.convert(amazonEC2.describeVolumes().getVolumes()));
            result.addAll(getHistoryVolumes());
            LOG.debug("Volume list: [{}]", result);
            return result;
        } catch (RuntimeException e) {
            LOG.error("Failed to get volume list.", e);
            throw new DataAccessException("Failed to get volume list.", e);
        }
    }

    private Set<VolumeDto> getHistoryVolumes() {
        return convert(backupRepository.findAll());
    }

    private Set<VolumeDto> convert(Iterable<BackupEntry> entries) {
        Set<VolumeDto> dtos = new HashSet<>();

        for (BackupEntry entry : entries) {
            VolumeDto dto = new VolumeDto();

            dto.setVolumeId(entry.getVolumeId());
            dto.setSize(0);

            dto.setInstanceID(EMPTY);
            dto.setVolumeName(EMPTY);
            dto.setTags(Collections.EMPTY_LIST);
            dto.setAvailabilityZone(EMPTY);
            dto.setSnapshotId(EMPTY);
            dto.setState(REMOVED_VOLUME_STATE);

            dtos.add(dto);
        }

        return dtos;
    }

    private static final VolumeDtoComparator volumeDtoComparator = new VolumeDtoComparator();

    private static final class VolumeDtoComparator implements Comparator<VolumeDto> {
        @Override
        public int compare(VolumeDto o1, VolumeDto o2) {
            return o1.getVolumeId().compareTo(o2.getVolumeId());
        }
    }
}
