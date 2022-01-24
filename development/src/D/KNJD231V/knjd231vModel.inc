<?php

require_once('for_php7.php');


class knjd231vModel extends Model {
    
    var $field = array();
    var $data  = array();
    var $cmd;
    var $absent_cov;        //欠課数換算
    var $absent_cov_late;   //欠課数換算遅刻
    var $amari_kuriage;     //余り繰上
    var $jugyou_jisu_flg;   //1：法定時数・2：実時数

    /**
     * メンバ変数の初期化
     */
    function knjd231vModel()
    {
    }

    function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }

        $this->field = array("GRADE"                        =>  VARS::post("GRADE"),                        //学年
                            "GAKKI2"                        =>  VARS::post("GAKKI2"),                       //学期
                            "SUBCLASSCD"                    =>  VARS::post("SUBCLASSCD"),               //教科・科目
                            "KYOUKA_SOUGOU1"                =>  VARS::post("KYOUKA_SOUGOU1"),               //教科・科目
                            "KYOUKA_SOUGOU2"                =>  VARS::post("KYOUKA_SOUGOU2"),               //総合的な時間
                            "SEISEKI_HUSIN1"                =>  VARS::post("SEISEKI_HUSIN1"),               //成績不振者チェックボックス
                            "SEISEKI_HUSIN2"                =>  VARS::post("SEISEKI_HUSIN2"),               //成績不振者チェックボックス
                            "SEISEKI_HUSIN3"                =>  VARS::post("SEISEKI_HUSIN3"),               //成績不振者チェックボックス
                            "SEISEKI_HUSIN_HYOUTEI_FROM"    =>  VARS::post("SEISEKI_HUSIN_HYOUTEI_FROM"),   //成績不振者 評定
                            "SEISEKI_HUSIN_HYOUTEI_TO"      =>  VARS::post("SEISEKI_HUSIN_HYOUTEI_TO"),     //成績不振者 評定
                            "DATE"                          =>  VARS::post("DATE"),                         //異動対象日付
                            "DBNAME"                        =>  VARS::post("DBNAME")                        //DB変更に備えて
                     );

        $this->cmd     = VARS::request("cmd");

        // 遅刻何回で欠課とするかの指数取得
        $absent = knjd231vQuery::getScAbsentCov();
        $this->absent_cov      = $absent["ABSENT_COV"];
        $this->absent_cov_late = $absent["ABSENT_COV_LATE"];
        $this->amari_kuriage   = $absent["AMARI_KURIAGE"];

        $db = Query::dbCheckOut();
        $query = knjd231vQuery::getJugyouJisuFlg();
        $this->jugyou_jisu_flg = $db->getOne($query);
        Query::dbCheckIn($db);
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput()
    {
        return true;
    }

    function &getUpdateModel()
    {
        //配列
        $term = array("UPDATE_DATA","SCORE","COMP_CREDIT","GET_CREDIT");
        //初期化
        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            for ($j = 0; $j < get_count($term); $j++) {
                $term_name = $term[$j]."-".$i;
                $this->fields[$term[$j]][] = VARS::post($term_name);
            }
        }

        if ($this->validateInput()) {
            knjd231vQuery::update($this);
            $this->setMessage("MSG201");
        }
    }
}
//var_dump($_REQUEST);
?>
