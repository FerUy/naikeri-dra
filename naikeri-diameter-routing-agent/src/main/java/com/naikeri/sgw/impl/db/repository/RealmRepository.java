package com.naikeri.sgw.impl.db.repository;

import com.naikeri.sgw.impl.db.entity.Realm;

import java.util.List;

public interface RealmRepository {

    void saveRealm(Realm realm);
    List<Realm> findAll();
}
