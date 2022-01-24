<?php

require_once('for_php7.php');

    //定数定義
//配列内の数値のみ取得
function num_filter($var) {
    return (is_numeric($var));
}

class knje441Model extends Model {
    var $cmd;
    var $field;
    var $gc_select;                     //セレクトメニュー
    var $control_data = array();        //コントロールのデータを保持
    var $staffcd;                       //セキュリティーチェック
    var $sec_competence;                //権限保持
    var $updated = array();             //UPDATED値保持
    var $fields = array();
    var $hyoteiYomikae; // 評定1の場合は2で処理する
    var $schoolcd;  //学校コード
    var $execute_date; //県への報告用登録日付

    function knje441Model() {
        //学籍処理日取得
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init() {
        $this->field = array("TR_DATE"      =>  VARS::post("TR_DATE"),      //異動対象日
                             "DBNAME"       =>  VARS::post("DBNAME")        //DB変更に備えて
                             );

        $this->cmd           = VARS::request("cmd");
        $this->gc_select     = VARS::post("gc_select");        //プルダウンメニュー
        $this->hyoteiYomikae = VARS::post("hyoteiYomikae");
        $this->execute_date  = VARS::post("EXECUTE_DATE");
        $this->dataDiv       = VARS::post("DATA_DIV");
        $this->fixedDate     = VARS::post("FIXED_DATE");
        $this->docNumber     = VARS::post("DOC_NUMBER");
        $this->idouDate      = VARS::post("TR_DATE");
        $this->fixedData     = VARS::post("FIXED_DATA");


        //セキュリティーチェック
        $this->sec_competence = common::SecurityCheck($this->staffcd,"KNJE441");

        $this->updField = array();
        for ($i = 0; $i < get_count($this->fields["CODE"]); $i++) {
            $code_arr = explode("-", $this->fields["CODE"][$i]);
            foreach ($this->dataField as $key => $divSeq) {
                $setDivSeq = $divSeq."_".$code_arr[0]."_".$code_arr[1];
                $this->updField[$setDivSeq] = VARS::post("DATA{$setDivSeq}");
            }
        }

        //V_SCHOOL_MSTから学校コードを取得
        $this->schoolcd = knje441Query::getSchoolMst();

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        $this->update();
        $this->setMessage("MSG201");
    }

    //アップデート時の内部メソッド
    function &update() {
        knje441Query::getUpdateCollegeQuery($this);
        knje441Query::getUpdateCollege2Query($this);
        knje441Query::getUpdateSenmonQuery($this);
        knje441Query::getUpdateSenmon2Query($this);
        return;
    }

    //確定
    function &getFixedUpdateModel() {
        $this->fixedUpdate();
        $this->setMessage("MSG201");
    }

    //アップデート
    function &fixedUpdate() {
        knje441Query::getFixedUpdateQuery($this);
        return;
    }

    //県への報告
    function &getUpdateEdboardModel() {
        //県側・疾病等結果一覧テーブル
        //学校側・報告テーブル
        knje441Query::getUpdateReport($this);
        $this->setMessage("MSG201");
    }

    //ＣＳＶ出力
    function getDownloadModel() {
        $this->header_data = false;
        $contents  = $this->getCollegeCsv();
        $contents .= $this->getCollege2Csv();
        $contents .= $this->getSenmonCsv();
        $contents .= $this->getSenmon2Csv();
        //ＣＳＶ出力
        if ($this->header_data) {
            $couse = CTRL_YEAR."大学短大等進学状況 ";
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //ＣＳＶ出力
    function getCollegeCsv() {
        $contents  = "";    //クリア
        $contents .= CTRL_YEAR."年度 大学短大等進学状況\n";

        $header = array();
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "大学(学部)";
        $header[] = "";
        $header[] = "";
        $header[] = "短大(本科)";
        $header[] = "";
        $header[] = "";
        $header[] = "大学・短大(通信制)";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";

        $header2 = array();
        $header2[] = "学科名";
        $header2[] = "性別";
        $header2[] = "卒業者数";
        $header2[] = "国立";
        $header2[] = "公立";
        $header2[] = "私立";
        $header2[] = "国立";
        $header2[] = "公立";
        $header2[] = "私立";
        $header2[] = "国立";
        $header2[] = "公立";
        $header2[] = "私立";
        $header2[] = "放送大学";
        $header2[] = "大学・短大(別科)";
        $header2[] = "専攻科";
        $header2[] = "合計";

        $contents .= implode($header, ",")."\n";
        $contents .= implode($header2, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //課程学科性別取得
        $table = ($this->fixedData) ? "AFT_DISEASE_ADDITION441_COLLEGE_FIXED_DAT" : "AFT_DISEASE_ADDITION441_COLLEGE_DAT";
        $query = knje441Query::getCsvMajor($this, $table);
        $this->majorSex = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->majorSex[] = $row;
        }
        $result->free();

        $this->dataDiv = array();
        $this->dataDiv[] = array("SCHOOL_SORT" => "00", "SCHOOL_GROUP" => "00");
        $this->dataDiv[] = array("SCHOOL_SORT" => "01", "SCHOOL_GROUP" => "01");
        $this->dataDiv[] = array("SCHOOL_SORT" => "01", "SCHOOL_GROUP" => "02");
        $this->dataDiv[] = array("SCHOOL_SORT" => "01", "SCHOOL_GROUP" => "03");
        $this->dataDiv[] = array("SCHOOL_SORT" => "02", "SCHOOL_GROUP" => "01");
        $this->dataDiv[] = array("SCHOOL_SORT" => "02", "SCHOOL_GROUP" => "02");
        $this->dataDiv[] = array("SCHOOL_SORT" => "02", "SCHOOL_GROUP" => "03");
        $this->dataDiv[] = array("SCHOOL_SORT" => "03", "SCHOOL_GROUP" => "01");
        $this->dataDiv[] = array("SCHOOL_SORT" => "03", "SCHOOL_GROUP" => "02");
        $this->dataDiv[] = array("SCHOOL_SORT" => "03", "SCHOOL_GROUP" => "03");
        $this->dataDiv[] = array("SCHOOL_SORT" => "04", "SCHOOL_GROUP" => "00");
        $this->dataDiv[] = array("SCHOOL_SORT" => "05", "SCHOOL_GROUP" => "00");
        $this->dataDiv[] = array("SCHOOL_SORT" => "06", "SCHOOL_GROUP" => "00");

        $allData = array();
        foreach ($this->majorSex as $msKey => $msVal) {
            $totalCnt = 0;
            $contents .= "{$msVal["MAJORNAME"]},{$msVal["ABBV1"]}";
            foreach ($this->dataDiv as $dataKey => $dataVal) {
                $query = knje441Query::getCollegeCnt($this, $msVal, $dataVal);
                $setCnt = $db->getOne($query);
                $setCnt = $setCnt ? $setCnt : 0;
                $contents .= ",{$setCnt}";
                if ($dataVal["SCHOOL_SORT"] != "00") {
                    $totalCnt += (int)$setCnt;
                }
                $allData[$dataVal["SCHOOL_SORT"].$dataVal["SCHOOL_GROUP"]] += $setCnt;
            }
            $contents .= ",{$totalCnt}\n";
            $allData["9900"] += (int)$totalCnt;
            $this->header_data = true;
        }
        Query::dbCheckIn($db);
        $contents .= "合計,";
        foreach ($allData as $key => $val) {
            $contents .= ",{$val}";
        }

        return $contents."\n\n\n";
    }

    //ＣＳＶ出力
    function getCollege2Csv() {
        $contents  = "";    //クリア

        //DB接続
        $db = Query::dbCheckOut();

        $this->dataDiv = array();
        $this->dataDiv[] = array("SCHOOL_SORT" => "01", "NAME" => "大学(学部)");
        $this->dataDiv[] = array("SCHOOL_SORT" => "02", "NAME" => "短大(本科)");
        $this->dataDiv[] = array("SCHOOL_SORT" => "03", "NAME" => "大学・短大(通信制)");
        $this->dataDiv[] = array("SCHOOL_SORT" => "04", "NAME" => "放送大学");
        $this->dataDiv[] = array("SCHOOL_SORT" => "05", "NAME" => "大学・短大(別科)");
        $this->dataDiv[] = array("SCHOOL_SORT" => "06", "NAME" => "専攻科");
        $this->dataDiv[] = array("SCHOOL_SORT" => "ALL", "NAME" => "合計");

        $header = array();
        $header[] = "学科名";
        $header[] = "性別";

        //タイトル学部
        $query = knje441Query::getNameMst("E050");
        $this->titleE050 = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->titleE050[] = $row;
            $header[] = $row["NAME1"];
        }
        $result->free();
        $header[] = "計";

        //課程学科性別取得
        $table = ($this->fixedData) ? "AFT_DISEASE_ADDITION441_COLLEGE2_FIXED_DAT" : "AFT_DISEASE_ADDITION441_COLLEGE2_DAT";
        $query = knje441Query::getCsvMajor($this, $table);
        $this->majorSex = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->majorSex[] = $row;
        }
        $result->free();

        $totalCntAll = array();
        foreach ($this->dataDiv as $dataKey => $dataVal) {
            $allData = array();
            $contents .= CTRL_YEAR."年度 {$dataVal["NAME"]}\n";

            $contents .= implode($header, ",")."\n";

            foreach ($this->majorSex as $msKey => $msVal) {
                $totalCnt = 0;
                $contents .= "{$msVal["MAJORNAME"]},{$msVal["ABBV1"]}";
                foreach ($this->titleE050 as $e050Key => $e050Val) {
                    if ($dataVal["SCHOOL_SORT"] != "ALL") {
                        $query = knje441Query::getCollege2Cnt($this, $msVal, $dataVal, $e050Val);
                        $setCnt = $db->getOne($query);
                        $setCnt = $setCnt ? $setCnt : 0;
                        $contents .= ",{$setCnt}";
                        $totalCnt += (int)$setCnt;
                        $totalCntAll[$msVal["COURSECD"]][$msVal["MAJORCD"]][$msVal["SEX"]][$e050Val["NAMECD2"]] += (int)$setCnt;
                        $allData[$e050Val["NAMECD2"]] += (int)$setCnt;
                    } else {
                        $setCnt = $totalCntAll[$msVal["COURSECD"]][$msVal["MAJORCD"]][$msVal["SEX"]][$e050Val["NAMECD2"]];
                        $totalCnt += (int)$setCnt;
                        $contents .= ",{$setCnt}";
                        $allData[$e050Val["NAMECD2"]] += (int)$setCnt;
                    }
                }
                $contents .= ",{$totalCnt}\n";
                $allData["9999"] += (int)$totalCnt;
                $this->header_data = true;
            }
            $contents .= "合計,";
            foreach ($allData as $key => $val) {
                $contents .= ",{$val}";
            }
            $contents .= "\n\n";
        }

        Query::dbCheckIn($db);

        return $contents."\n\n";
    }

    //ＣＳＶ出力
    function getSenmonCsv() {
        $contents  = "";    //クリア
        $contents .= CTRL_YEAR."年度 専修・各種学校等入学状況\n";

        $header = array();
        $header[] = "学科名";
        $header[] = "性別";
        $header[] = "専修学校";
        $header[] = "各種学校";
        $header[] = "a合計";
        $header[] = "b公共職業訓練施設等";
        $header[] = "a + b合計";

        $contents .= implode($header, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //課程学科性別取得
        $table = ($this->fixedData) ? "AFT_DISEASE_ADDITION441_SENMON_FIXED_DAT" : "AFT_DISEASE_ADDITION441_SENMON_DAT";
        $query = knje441Query::getCsvMajor($this, $table);
        $this->majorSex = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->majorSex[] = $row;
        }
        $result->free();

        $this->dataDiv = array();
        $this->dataDiv[] = array("SCHOOL_SORT" => "11");
        $this->dataDiv[] = array("SCHOOL_SORT" => "12");
        $this->dataDiv[] = array("SCHOOL_SORT" => "ALL1");
        $this->dataDiv[] = array("SCHOOL_SORT" => "13");
        $this->dataDiv[] = array("SCHOOL_SORT" => "ALL2");

        $allData = array();
        foreach ($this->majorSex as $msKey => $msVal) {
            $totalCnt = 0;
            $totalAllCnt = 0;
            $contents .= "{$msVal["MAJORNAME"]},{$msVal["ABBV1"]}";
            foreach ($this->dataDiv as $dataKey => $dataVal) {
                if ($dataVal["SCHOOL_SORT"] != "ALL1" && $dataVal["SCHOOL_SORT"] != "ALL2") {
                    $query = knje441Query::getSenmonCnt($this, $msVal, $dataVal);
                    $setCnt = $db->getOne($query);
                    $setCnt = $setCnt ? $setCnt : 0;
                    $contents .= ",{$setCnt}";
                    $totalAllCnt += (int)$setCnt;
                    if ($dataVal["SCHOOL_SORT"] != "13") {
                        $totalCnt += (int)$setCnt;
                    }
                    $allData[$dataVal["SCHOOL_SORT"]] += (int)$setCnt;
                } else if ($dataVal["SCHOOL_SORT"] == "ALL1") {
                    $contents .= ",{$totalCnt}";
                    $allData[$dataVal["SCHOOL_SORT"]] += (int)$totalCnt;
                } else if ($dataVal["SCHOOL_SORT"] == "ALL2") {
                    $contents .= ",{$totalAllCnt}";
                    $allData[$dataVal["SCHOOL_SORT"]] += (int)$totalCnt;
                }
            }
            $contents .= "\n";
            $this->header_data = true;
        }
        Query::dbCheckIn($db);
        $contents .= "合計,";
        foreach ($allData as $key => $val) {
            $contents .= ",{$val}";
        }

        return $contents."\n\n\n";
    }

    //ＣＳＶ出力
    function getSenmon2Csv() {
        $contents  = "";    //クリア

        //DB接続
        $db = Query::dbCheckOut();

        $contents .= CTRL_YEAR."年度 専修・各種学校等系統別人数\n";

        $header = array();
        $header[] = "学科名";
        $header[] = "性別";

        //タイトル学部
        $query = knje441Query::getNameMst("E051");
        $this->titleE051 = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->titleE051[] = $row;
            $header[] = $row["NAME1"];
        }
        $result->free();
        $header[] = "合計";
        $contents .= implode($header, ",")."\n";

        //課程学科性別取得
        $table = ($this->fixedData) ? "AFT_DISEASE_ADDITION441_COLLEGE2_FIXED_DAT" : "AFT_DISEASE_ADDITION441_COLLEGE2_DAT";
        $query = knje441Query::getCsvMajor($this, $table);
        $this->majorSex = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->majorSex[] = $row;
        }
        $result->free();

        $allData = array();
        foreach ($this->majorSex as $msKey => $msVal) {
            $totalCnt = 0;
            $contents .= "{$msVal["MAJORNAME"]},{$msVal["ABBV1"]}";
            foreach ($this->titleE051 as $e051Key => $e051Val) {
                $query = knje441Query::getSenmon2Cnt($this, $msVal, $e051Val);
                $setCnt = $db->getOne($query);
                $setCnt = $setCnt ? $setCnt : 0;
                $contents .= ",{$setCnt}";
                $totalCnt += (int)$setCnt;
                $allData[$e051Val["NAMECD2"]] += (int)$setCnt;
            }
            $contents .= ",{$totalCnt}\n";
            $allData["9999"] += (int)$totalCnt;
            $this->header_data = true;
        }
        Query::dbCheckIn($db);
        $contents .= "合計,";
        foreach ($allData as $key => $val) {
            $contents .= ",{$val}";
        }

        return $contents."\n\n\n";
    }
}
?>
