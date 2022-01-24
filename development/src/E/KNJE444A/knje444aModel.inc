<?php

require_once('for_php7.php');
class knje444aModel extends Model
{
    public $cmd;
    public $item;
    public $selectdata_l;              //対象学校
    public $selectdata_r;              //書出し項目一覧

    public function knja150Model()
    {
    }

    public function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //出力対象年度
        $this->year             = VARS::post("YEAR");
        //ITEM
        $this->selectdata_r     = VARS::post("selectdata_r");
        //対象学校
        $this->selectdata_l     = VARS::post("selectdata_l");

        //項目一覧
        $this->item = array();
        $this->item[]["EDBOARD_SCHOOLCD"]   = "学校コード";
        $this->item[]["SCHOOLNAME"]         = "学校名称";
        $this->item[]["YEAR"]               = "年度";
        $this->item[]["TOROKU_DATE"]        = "登録日";
        $this->item[]["SEQ"]                = "SEQ";
        //項目一覧（生徒データ）
        $this->item[]["SCHREGNO"]           = "学籍番号";
        $this->item[]["MAJORCD"]            = "学科コード";
        $this->item[]["MAJORNAME"]          = "学科名";
        $this->item[]["GRADE"]              = "学年";
        $this->item[]["HR_CLASS_NAME"]      = "組";
        $this->item[]["ATTENDNO"]           = "出席番号";
        $this->item[]["NAME"]               = "生徒氏名";
        $this->item[]["NAME_KANA"]          = "生徒氏名かな";
        $this->item[]["BIRTHDAY"]           = "生年月日";
        $this->item[]["SEX"]                = "性別";
        $this->item[]["FINSCHOOLCD"]        = "出身校コード";
        $this->item[]["FINSCHOOLNAME"]      = "出身校名称";
        $this->item[]["PREF_CD"]            = "出身校都道府県コード";
        $this->item[]["PREF_NAME"]          = "出身校都道府県名";
        //項目一覧（進路データ）
        $this->item[]["SENKOU_KIND"]        = "受験先種別コード";
        $this->item[]["SENKOU_NAME"]        = "受験先種別名称";
        $this->item[]["STAT_CD"]            = "受験先コード";
        $this->item[]["STAT_NAME"]          = "受験先名";
        $this->item[]["SCHOOL_GROUP"]       = "学校系列コード";
        $this->item[]["SCHOOL_GROUP_NAME"]  = "学校系列名";
        $this->item[]["FACULTYCD"]          = "学部コード";
        $this->item[]["FACULTYNAME"]        = "学部名";
        $this->item[]["DEPARTMENTCD"]       = "学科コード";
        $this->item[]["DEPARTMENTNAME"]     = "学科名";
        $this->item[]["JOBTYPE_LCD"]        = "職種コード（大）";
        $this->item[]["JOBTYPE_LNAME"]      = "職種（大）名";
        $this->item[]["JOBTYPE_MCD"]        = "職種コード（中）";
        $this->item[]["JOBTYPE_MNAME"]      = "職種（中）名";
        $this->item[]["JOBTYPE_SCD"]        = "職種コード（小）";
        $this->item[]["JOBTYPE_SSCD"]       = "職種コード（細）";
        $this->item[]["JOBTYPE_SNAME"]      = "職種（小細）名";
        $this->item[]["AFT_GRAD_PREF_CD"]   = "進学・就職先都道府県コード";
        $this->item[]["AFT_GRAD_PREF_NAME"] = "進学・就職先都道府県名";
        $this->item[]["HOWTOEXAM"]          = "受験方式コード";
        $this->item[]["HOWTOEXAM_NAME"]     = "受験方式名";
        $this->item[]["DECISION"]           = "受験結果コード";
        $this->item[]["DECISION_NAME"]      = "受験結果名";
        $this->item[]["PLANSTAT"]           = "進路状況コード";
        $this->item[]["PLANSTAT_NAME"]      = "進路状況名";
        //項目一覧（評定データ）
        for ($annSeq = 1; $annSeq <= 4; $annSeq++) {
            $this->item[]["AVG".$annSeq]    = $annSeq."年評定平均";
        }
        for ($subSeq = 1; $subSeq <= 40; $subSeq++) {
            $this->item[]["SUBCLASSCD_".$subSeq]    = "科目コード".$subSeq;
            $this->item[]["SUBCLASSNAME_".$subSeq]  = "科目コード".$subSeq."・"."科目名";
            for ($annSeq = 1; $annSeq <= 4; $annSeq++) {
                $this->item[]["SUBCLASS".$subSeq."_VALUE".$annSeq] = "科目コード".$subSeq."・".$annSeq."年評定";
            }
        }
        $this->item[]["SUBCLASS_OVERFLG"]   = "科目数超過";
    }

    //CSV書出し
    public function getCsvModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //出力対象校一覧
        $schoolcd = explode(",", $this->selectdata_l);

        //設定内容保存
        $query = knje444aQuery::getDelFieldSql($this, "02");
        $db->query($query);
        $setSeq = 1;
        foreach ($schoolcd as $field) {
            $query = knje444aQuery::getInsFieldSql($this, "02", $setSeq, $field);
            $db->query($query);
            $setSeq++;
        }

        //書出し項目一覧
        $item = explode(",", $this->selectdata_r);

        //設定内容保存
        $query = knje444aQuery::getDelFieldSql($this, "01");
        $db->query($query);
        $setSeq = 1;
        foreach ($item as $field) {
            $query = knje444aQuery::getInsFieldSql($this, "01", $setSeq, $field);
            $db->query($query);
            $setSeq++;
        }

        //項目名一覧
        $item_array = array();
        foreach ($this->item as $key => $val) {
            foreach ($val as $field => $label) {
                $item_array[$field] = $label;
            }
        }

        //初期化
        $dataFlg = false; //出力フラグ
        $contents = "";   //クリア

        //ヘッダ部
        $sep = "";
        foreach ($item as $field) {
            $contents .= $sep.$item_array[$field];
            $sep = ",";
        }
        $contents .= "\n";

        //データ部
        $result = $db->query(knje444aQuery::getCsvQuery($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $sep = "";
            foreach ($item as $field) {
                if (in_array($field, array("TOROKU_DATE", "BIRTHDAY"))) {
                    $row[$field] = str_replace("-", "/", $row[$field]);
                }
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";

            $dataFlg = true;
        }

        //DB切断
        Query::dbCheckIn($db);

        if ($dataFlg) {
            $filename = sprintf("%s.csv", "進路統計情報");
            common::downloadContents($contents, $filename);
            exit;
        } else {
            $this->warning = "出力データはありません。";
            return false;
        }
    }
}
