package com.sungardas.snapdirector.service.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import com.sungardas.snapdirector.aws.dynamodb.model.BackupEntry;
import com.sungardas.snapdirector.aws.dynamodb.model.RetentionEntry;
import com.sungardas.snapdirector.aws.dynamodb.repository.BackupRepository;
import com.sungardas.snapdirector.aws.dynamodb.repository.RetentionRepository;
import com.sungardas.snapdirector.service.BackupService;
import com.sungardas.snapdirector.service.VolumeService;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.sungardas.snapdirector.aws.dynamodb.model.BackupState.COMPLETED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RetentionServiceImplTest {

    @Mock
    private RetentionRepository retentionRepository;

    @Mock
    private BackupRepository backupRepository;

    @Mock
    private BackupService backupService;

    @Mock
    private VolumeService volumeService;

    @Captor
    private ArgumentCaptor<Collection<BackupEntry>> backupCollectionArgumentCaptor;

    @Captor
    private ArgumentCaptor<String> userArgumentCaptor;

    @Captor
    private ArgumentCaptor<Iterable<RetentionEntry>> retentionCollectionArgumentCaptor;

    @InjectMocks
    private RetentionServiceImpl retentionService;

    @Test
    public void applyTest() {
        BackupEntry entry1 = new BackupEntry("volumeId 1", "fileName1", new DateTime().plusHours(1).getMillis() + "", "1", COMPLETED, "", "", "", "", "");
        BackupEntry entry2 = new BackupEntry("volumeId 1", "fileName2", new DateTime().plusDays(1).getMillis() + "", "1", COMPLETED, "", "", "", "", "");
        BackupEntry entry3 = new BackupEntry("volumeId 1", "fileName3", new DateTime().plusDays(2).getMillis() + "", "1", COMPLETED, "", "", "", "", "");
        when(backupRepository.findAll(null)).thenReturn(Arrays.asList(entry1, entry2, entry3));

        RetentionEntry retentionEntry = new RetentionEntry("volumeId 1", 2, 1, 1);
        when(retentionRepository.findByInstanceId(null)).thenReturn(Arrays.asList(retentionEntry));

        retentionService.apply();

        verify(backupService).deleteBackup(backupCollectionArgumentCaptor.capture(), userArgumentCaptor.capture());

        Collection<BackupEntry> backupEntries = backupCollectionArgumentCaptor.getValue();

        assertNotNull(backupEntries);

        assertEquals(2, backupEntries.size());

        Collection<BackupEntry> collection = backupCollectionArgumentCaptor.getValue();

        assertTrue(collection.contains(entry1));
        assertTrue(collection.contains(entry2));
        assertFalse(collection.contains(entry3));
        assertEquals(RetentionServiceImpl.RETENTION_USER, userArgumentCaptor.getValue());
    }


    @Test
    public void applyCountRetentionPolicyTest() {
        BackupEntry entry1 = new BackupEntry("volumeId 1", "fileName1", new DateTime().plusHours(1).getMillis() + "", "1", COMPLETED, "", "", "", "", "");
        BackupEntry entry2 = new BackupEntry("volumeId 1", "fileName2", new DateTime().plusDays(1).getMillis() + "", "1", COMPLETED, "", "", "", "", "");
        BackupEntry entry3 = new BackupEntry("volumeId 1", "fileName3", new DateTime().plusDays(2).getMillis() + "", "1", COMPLETED, "", "", "", "", "");
        when(backupRepository.findAll(null)).thenReturn(Arrays.asList(entry1, entry2, entry3));

        RetentionEntry retentionEntry = new RetentionEntry("volumeId 1", 0, 1, 0);
        when(retentionRepository.findByInstanceId(null)).thenReturn(Arrays.asList(retentionEntry));

        retentionService.apply();

        verify(backupService).deleteBackup(backupCollectionArgumentCaptor.capture(), userArgumentCaptor.capture());

        Collection<BackupEntry> backupEntries = backupCollectionArgumentCaptor.getValue();

        assertNotNull(backupEntries);

        assertEquals(2, backupEntries.size());

        Iterator<BackupEntry> iterator = backupCollectionArgumentCaptor.getValue().iterator();

        assertEquals(entry2, iterator.next());
        assertEquals(entry1, iterator.next());
        assertEquals(RetentionServiceImpl.RETENTION_USER, userArgumentCaptor.getValue());
    }

    @Test
    public void applySizeRetentionPolicyTest1() {
        BackupEntry entry1 = new BackupEntry("volumeId 1", "fileName1", new DateTime().plusHours(1).getMillis() + "", "1073741824", COMPLETED, "", "", "", "", "");
        BackupEntry entry2 = new BackupEntry("volumeId 1", "fileName2", new DateTime().plusDays(1).getMillis() + "", "1073741824", COMPLETED, "", "", "", "", "");
        BackupEntry entry3 = new BackupEntry("volumeId 1", "fileName3", new DateTime().plusDays(2).getMillis() + "", "1073741824", COMPLETED, "", "", "", "", "");
        when(backupRepository.findAll(null)).thenReturn(Arrays.asList(entry1, entry2, entry3));

        RetentionEntry retentionEntry = new RetentionEntry("volumeId 1", 1, 0, 0);
        when(retentionRepository.findByInstanceId(null)).thenReturn(Arrays.asList(retentionEntry));

        retentionService.apply();

        verify(backupService).deleteBackup(backupCollectionArgumentCaptor.capture(), userArgumentCaptor.capture());

        Collection<BackupEntry> backupEntries = backupCollectionArgumentCaptor.getValue();

        assertNotNull(backupEntries);

        assertEquals(2, backupEntries.size());

        Iterator<BackupEntry> iterator = backupCollectionArgumentCaptor.getValue().iterator();

        assertEquals(entry2, iterator.next());
        assertEquals(entry1, iterator.next());
        assertEquals(RetentionServiceImpl.RETENTION_USER, userArgumentCaptor.getValue());
    }

    @Test
    public void applySizeRetentionPolicyTest2() {
        BackupEntry entry1 = new BackupEntry("volumeId 1", "fileName1", new DateTime().plusHours(1).getMillis() + "", "1073741824", COMPLETED, "", "", "", "", "");
        BackupEntry entry2 = new BackupEntry("volumeId 1", "fileName2", new DateTime().plusDays(1).getMillis() + "", "1073741824", COMPLETED, "", "", "", "", "");
        BackupEntry entry3 = new BackupEntry("volumeId 1", "fileName3", new DateTime().plusDays(2).getMillis() + "", "1073741824", COMPLETED, "", "", "", "", "");
        when(backupRepository.findAll(null)).thenReturn(Arrays.asList(entry1, entry2, entry3));

        RetentionEntry retentionEntry = new RetentionEntry("volumeId 1", 2, 0, 0);
        when(retentionRepository.findByInstanceId(null)).thenReturn(Arrays.asList(retentionEntry));

        retentionService.apply();

        verify(backupService).deleteBackup(backupCollectionArgumentCaptor.capture(), userArgumentCaptor.capture());

        Collection<BackupEntry> backupEntries = backupCollectionArgumentCaptor.getValue();

        assertNotNull(backupEntries);

        assertEquals(1, backupEntries.size());

        Collection<BackupEntry> collection = backupCollectionArgumentCaptor.getValue();

        assertTrue(collection.contains(entry1));
        assertEquals(RetentionServiceImpl.RETENTION_USER, userArgumentCaptor.getValue());
    }

    @Test
    public void applyDayRetentionPolicyTest1() {
        BackupEntry entry1 = new BackupEntry("volumeId 1", "fileName1", new DateTime().minusDays(2).getMillis() + "", "1", COMPLETED, "", "", "", "", "");
        BackupEntry entry2 = new BackupEntry("volumeId 1", "fileName2", new DateTime().plusHours(23).getMillis() + "", "1", COMPLETED, "", "", "", "", "");
        BackupEntry entry3 = new BackupEntry("volumeId 1", "fileName3", new DateTime().plusDays(2).getMillis() + "", "1", COMPLETED, "", "", "", "", "");
        when(backupRepository.findAll(null)).thenReturn(Arrays.asList(entry1, entry2, entry3));

        RetentionEntry retentionEntry = new RetentionEntry("volumeId 1", 0, 0, 1);
        when(retentionRepository.findByInstanceId(null)).thenReturn(Arrays.asList(retentionEntry));

        retentionService.apply();

        verify(backupService).deleteBackup(backupCollectionArgumentCaptor.capture(), userArgumentCaptor.capture());

        Collection<BackupEntry> backupEntries = backupCollectionArgumentCaptor.getValue();

        assertNotNull(backupEntries);

        assertEquals(1, backupEntries.size());

        Iterator<BackupEntry> iterator = backupCollectionArgumentCaptor.getValue().iterator();

        assertEquals(entry1, iterator.next());
        assertEquals(RetentionServiceImpl.RETENTION_USER, userArgumentCaptor.getValue());
    }

    @Test
    public void applyDayRetentionPolicyTest2() {
        BackupEntry entry1 = new BackupEntry("volumeId 1", "fileName1", new DateTime().minusDays(2).getMillis() + "", "1", COMPLETED, "", "", "", "", "");
        BackupEntry entry2 = new BackupEntry("volumeId 1", "fileName2", new DateTime().minusDays(23).getMillis() + "", "1", COMPLETED, "", "", "", "", "");
        BackupEntry entry3 = new BackupEntry("volumeId 1", "fileName3", new DateTime().plusDays(2).getMillis() + "", "1", COMPLETED, "", "", "", "", "");
        when(backupRepository.findAll(null)).thenReturn(Arrays.asList(entry1, entry2, entry3));

        RetentionEntry retentionEntry = new RetentionEntry("volumeId 1", 0, 0, 1);
        when(retentionRepository.findByInstanceId(null)).thenReturn(Arrays.asList(retentionEntry));

        retentionService.apply();

        verify(backupService).deleteBackup(backupCollectionArgumentCaptor.capture(), userArgumentCaptor.capture());

        Collection<BackupEntry> backupEntries = backupCollectionArgumentCaptor.getValue();

        assertNotNull(backupEntries);

        assertEquals(2, backupEntries.size());

        Collection<BackupEntry> collection = backupCollectionArgumentCaptor.getValue();

        assertTrue(collection.contains(entry1));
        assertTrue(collection.contains(entry2));
        assertEquals(RetentionServiceImpl.RETENTION_USER, userArgumentCaptor.getValue());
    }

    @Test
    public void removeEmptyRetentionsTest() {
        BackupEntry entry1 = new BackupEntry("volumeId 1", "fileName1", new DateTime().minusDays(2).getMillis() + "", "1", COMPLETED, "", "", "", "", "");
        BackupEntry entry2 = new BackupEntry("volumeId 2", "fileName2", new DateTime().minusDays(23).getMillis() + "", "1", COMPLETED, "", "", "", "", "");
        BackupEntry entry3 = new BackupEntry("volumeId 3", "fileName3", new DateTime().plusDays(2).getMillis() + "", "1", COMPLETED, "", "", "", "", "");
        when(backupRepository.findAll(null)).thenReturn(Arrays.asList(entry1, entry2, entry3));

        RetentionEntry retentionEntry = new RetentionEntry("volumeId 1", 2, 0, 0);
        RetentionEntry retentionEntry2 = new RetentionEntry("volumeId 2", 0, 0, 0);
        RetentionEntry retentionEntry4 = new RetentionEntry("volumeId 4", 0, 0, 0);
        when(retentionRepository.findByInstanceId(null)).thenReturn(Arrays.asList(retentionEntry, retentionEntry2, retentionEntry4));

        retentionService.apply();

        verify(retentionRepository).delete(RetentionEntry.getId("volumeId 2", null));
        verify(retentionRepository).delete(RetentionEntry.getId("volumeId 4", null));
    }
}
