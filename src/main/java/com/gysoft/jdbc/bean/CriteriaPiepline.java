package com.gysoft.jdbc.bean;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author 周宁
 * @Date 2019-04-12 15:10
 */
public class CriteriaPiepline {

    private List<CriteriaNext> criteriaNextList = new ArrayList<>();

    public CriteriaPiepline(Criteria head) {
        criteriaNextList.add(new CriteriaNext(head,null));
    }

    public void add(Criteria criteria,String type){
        criteriaNextList.add(new CriteriaNext(criteria,type));
    }

    public List<CriteriaNext> getCriteriaNextList() {
        return criteriaNextList;
    }

    public static class CriteriaNext{

        private Criteria criteria;

        private String unionType;

        public CriteriaNext(Criteria criteria, String unionType) {
            this.criteria = criteria;
            this.unionType = unionType;
        }

        public Criteria getCriteria() {
            return criteria;
        }

        public void setCriteria(Criteria criteria) {
            this.criteria = criteria;
        }

        public String getUnionType() {
            return unionType;
        }

        public void setUnionType(String unionType) {
            this.unionType = unionType;
        }
    }
}
