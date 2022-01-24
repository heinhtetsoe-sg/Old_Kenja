<?php

require_once('for_php7.php');

class knje444Model extends Model
{
    public $field = array();
    public $cmd;

    public function knje444Model()
    {
    }

    public function init()
    {
        $this->field = array("YEAR"     =>  CTRL_YEAR,              //対象年度
                             "MAJORCD"  =>  VARS::post("MAJORCD"),  //対象学科
                             "DBNAME"   =>  VARS::post("DBNAME")    //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    public function clean()
    {
    }

    public function getDownloadCsvOrUpdateHoukokuModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //ヘッダ
        $header = array();
        $header["EDBOARD_SCHOOLCD"]     = "学校コード";
        $header["EDBOARD_SCHOOLNAME"]   = "学校名称";
        $header["YEAR"]                 = "年度";
        $header["TOROKU_DATE"]          = "登録日";
        $header["SEQ"]                  = "SEQ";
        //ヘッダ（生徒データ）
        $header["SCHREGNO"]             = "学籍番号";
        $header["MAJORCD"]              = "学科コード";
        $header["MAJORNAME"]            = "学科名";
        $header["GRADE"]                = "学年";
        $header["HR_CLASS_NAME"]        = "組";
        $header["ATTENDNO"]             = "出席番号";
        $header["NAME"]                 = "生徒氏名";
        $header["NAME_KANA"]            = "生徒氏名かな";
        $header["BIRTHDAY"]             = "生年月日";
        $header["SEX"]                  = "性別";
        $header["FINSCHOOLCD"]          = "出身校コード";
        $header["FINSCHOOL_NAME"]       = "出身校名称";
        $header["FINSCHOOL_PREF_CD"]    = "出身校都道府県コード";
        $header["FINSCHOOL_PREF_NAME"]  = "出身校都道府県名";
        //ヘッダ（進路データ）
        $header["SENKOU_KIND"]          = "受験先種別コード";
        $header["SENKOU_NAME"]          = "受験先種別名称";
        $header["STAT_CD"]              = "受験先コード";
        $header["STAT_NAME"]            = "受験先名";
        $header["SCHOOL_GROUP"]         = "学校系列コード";
        $header["SCHOOL_GROUP_NAME"]    = "学校系列名";
        $header["FACULTYCD"]            = "学部コード";
        $header["FACULTYNAME"]          = "学部名";
        $header["DEPARTMENTCD"]         = "学科コード";
        $header["DEPARTMENTNAME"]       = "学科名";
        $header["JOBTYPE_LCD"]          = "職種コード（大）";
        $header["JOBTYPE_LNAME"]        = "職種（大）名";
        $header["JOBTYPE_MCD"]          = "職種コード（中）";
        $header["JOBTYPE_MNAME"]        = "職種（中）名";
        $header["JOBTYPE_SCD"]          = "職種コード（小）";
        $header["JOBTYPE_SSCD"]         = "職種コード（細）";
        $header["JOBTYPE_SNAME"]        = "職種（小細）名";
        $header["AFT_GRAD_PREF_CD"]     = "進学・就職先都道府県コード";
        $header["AFT_GRAD_PREF_NAME"]   = "進学・就職先都道府県名";
        $header["HOWTOEXAM"]            = "受験方式コード";
        $header["HOWTOEXAM_NAME"]       = "受験方式名";
        $header["DECISION"]             = "受験結果コード";
        $header["DECISION_NAME"]        = "受験結果名";
        $header["PLANSTAT"]             = "進路状況コード";
        $header["PLANSTAT_NAME"]        = "進路状況名";
        //ヘッダ（評定データ）
        $header2 = array();
        for ($annSeq = 1; $annSeq <= 4; $annSeq++) {
            $header2["AVG".$annSeq] = $annSeq."年評定平均";
        }
        for ($subSeq = 1; $subSeq <= 40; $subSeq++) {
            $header2["SUBCLASSCD_".$subSeq] = "科目コード".$subSeq;
            $header2["SUBCLASSNAME_".$subSeq] = "科目コード".$subSeq."・"."科目名";
            for ($annSeq = 1; $annSeq <= 4; $annSeq++) {
                $header2["SUBCLASS".$subSeq."_VALUE".$annSeq] = "科目コード".$subSeq."・".$annSeq."年評定";
            }
        }
        $header2["SUBCLASS_OVERFLG"] = "科目数超過";

        //ヘッダ出力
        $header_data = false;   //出力フラグ
        $contents  = "";        //クリア
        $contents  = implode($header, ",") .",";
        $contents .= implode($header2, ",") ."\n";

        //SQL文発行（評定データ）・・・配列に保持しておく
        $studyrec = array();
        $query = knje444Query::getCsvStudyrec($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",", ".", $row);
            $row = str_replace("\r", "", $row);
            $row = str_replace("\n", "", $row);

            $studyrec[$row["SCHREGNO"]][$row["SUBCLASSCD"]][$row["ANNUAL"]] = $row;
        }
        $csv2 = array();
        foreach ($studyrec as $schregno => $array1) {
            $subSeq = 0;
            foreach ($array1 as $subclasscd => $array2) {
                $subSeq++;
                foreach ($array2 as $annual => $row) {
                    $annSeq = sprintf("%d", $row["ANNUAL"]);
                    $csv2[$schregno]["AVG".$annSeq] = $row["AVG"];                              //評定平均
                    $csv2[$schregno]["SUBCLASSCD_".$subSeq] = $row["SUBCLASSCD"];               //科目コード
                    $csv2[$schregno]["SUBCLASSNAME_".$subSeq] = $row["SUBCLASSNAME"];           //科目名
                    $csv2[$schregno]["SUBCLASS".$subSeq."_VALUE".$annSeq] = $row["VALUATION"];  //評定
                    $csv2[$schregno]["SUBCLASS_OVERFLG"] = ($subSeq > 40) ? "*" : "";           //科目数超過
                }
            }
        }

        //県への報告
        $houkokuList = array();

        //SQL文発行（生徒データ）（進路データ）
        $query = knje444Query::getCsvRegdAndAft($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",", ".", $row);
            $row = str_replace("\r", "", $row);
            $row = str_replace("\n", "", $row);

            //学校コード、学校名称
            $row["EDBOARD_SCHOOLCD"] = $this->schoolcd;
            $row["EDBOARD_SCHOOLNAME"] = $this->schoolname;

            //生徒データ
            //進路データ
            $sep = "";
            foreach ($header as $field => $label) {
                if (in_array($field, array("TOROKU_DATE", "BIRTHDAY"))) {
                    $row[$field] = str_replace("-", "/", $row[$field]);
                }
                $contents .= $sep.$row[$field];
                $sep = ",";
            }

            //評定データ
            foreach ($header2 as $field => $label) {
                if (is_array($csv2[$row["SCHREGNO"]])) {
                    $row[$field] = $csv2[$row["SCHREGNO"]][$field];
                } else {
                    $row[$field] = "";
                }
                $contents .= $sep.$row[$field];
                $sep = ",";
            }

            $contents .= "\n";
            $header_data = true;

            //県への報告
            $houkokuList[] = $row;
        }

        //ファイル名
        $title = "進路統計情報";

        //DB切断
        Query::dbCheckIn($db);

        if ($header_data && $this->cmd == "csv") {
            //CSV出力（データ確認）
            $title = "進路統計情報";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } elseif ($header_data && $this->cmd == "houkoku") {
            //県への報告
            //県側（格納先テーブル）、学校側（提出履歴記録用テーブル）
            knje444Query::getUpdateHoukoku($this, $houkokuList);
            $this->message = "提出が完了しました。";
            return false;
        } else {
            $this->warning = "出力データはありません。";
            return false;
        }
    }
}
