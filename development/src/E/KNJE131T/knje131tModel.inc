<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knje131tModel extends Model {
    var $field          = array();
    var $opt_classcd    = array();
    var $opt_subclasscd    = array();
    var $selectdata;
    var $selectdata2;
    var $schooldiv;
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード

    /**
     * メンバ変数の初期化
     */
    function knje131tModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"                =>  VARS::post("YEAR"),             //年度
                            "GAKKI"                =>  VARS::post("GAKKI"),            //学期コード
                            "GRADE"                =>  VARS::post("GRADE"),            //学年
                            "OUT_DIV"              =>  VARS::post("OUT_DIV"),          //対象ラジオボタン 
                            "HYOTEI_YOMIKAE"       =>  VARS::post("HYOTEI_YOMIKAE"),   //評定1の場合は2で処理する
                            "DBNAME"               =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd          = VARS::request("cmd");
        $this->selectdata   = VARS::post("selectdata");
        $this->selectdata2  = VARS::post("selectdata2");
        $this->schooldiv    = VARS::post("SCHOOLDIV");
        $this->getSchoolKind    = VARS::post("GET_SCHOOL_KIND");

        $this->csvOptCnt = "";
        if (VARS::post("CSV_OPT_CNT")) {
            $this->csvOptCnt = VARS::post("CSV_OPT_CNT");
            for ($i=1; $i <= $this->csvOptCnt; $i++) {
                $optName = "CSV_OPT".$i;
                $this->field[$optName] = VARS::post($optName);
            }
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
    /**
     * ＣＳＶ出力
     */
    function getDownloadModel()
    {
        //ヘッダ
        $header = array("YEAR"         => "年度",
                        "SCHREGNO"     => "学籍番号",
                        "GRADE"        => "学年",
                        "HR_CLASS"     => "組",
                        "ATTENDNO"     => "出席番号",
                        "NAME"         => "生徒氏名",
                        "SEX"          => "性別"
                        );

        if ($this->field["OUT_DIV"] == '1') {
            $header2 = array("VAL_5ALL"     => "主要教科の評価平均",
                             "VAL_ALL"      => "全教科の評価平均"
                            );
        } else {
            $header2 = array(
                             "GET_CREDIT"   => "修得単位数計",
                             "COMP_CREDIT"  => "履修単位数計",
                             "GRAD_CREDIT"  => "卒業単位数",
                             "ASSESSLEVEL"  => "学習成績概評",
                             "VAL_ALL"      => "全教科の評定平均",
                             "VAL_5ALL"     => "主要教科の評定平均"
                            );
        }

        //DB接続
        $db = Query::dbCheckOut();

        //科目
        $header3 = array();
        $query = knje131tQuery::getSubClassName($this);
        $result = $db->query($query);
        $this->opt_subclasscd = array();
        $setGradeName = "0" == $this->schooldiv ? "年生" : "年次";
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $header3[] = $row["SUBCLASSNAME"] . "(" . sprintf("%d",$row["ANNUAL"]) . $setGradeName . ")";
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $this->opt_subclasscd[] = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"] . "-" . $row["ANNUAL"];
            } else {
                $this->opt_subclasscd[] = $row["SUBCLASSCD"] . "-" . $row["ANNUAL"];
            }
        }

        //教科
        $header4 = array();
        $query = knje131tQuery::getClassName($this);
        $result = $db->query($query);
        $this->opt_classcd = array();
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $header4[] = $row["CLASSNAME"] . "平均";
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $this->opt_classcd[] = $row["CLASSCD"]."-".$row["SCHOOL_KIND"];
            } else {
                $this->opt_classcd[] = $row["CLASSCD"];
            }
        }
        $result->free();

        // 学年毎の名称取得
        $annualList = array();
        $query = knje131tQuery::getSelectAnnual($this);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $annualList[] = $row;
        }
        $result->free();


        //CSV_OPTのヘッダ設定
        $header5 = array();
        if ($this->field['CSV_OPT1']) {
            for ($i=0; $i < get_count($annualList); $i++) {
                $annualName = "(".sprintf("%d",$annualList[$i]['ANNUAL']).$setGradeName.")";
                $header5[] = "出欠".$annualName;
                $header5[] = "出停".$annualName;
                $header5[] = "遅刻・早退".$annualName;
                $header5[] = "総欠課時数".$annualName;
            }
        }
        if ($this->field['CSV_OPT2']) {
            for ($i=0; $i < get_count($annualList); $i++) {
                $annualName = "(".sprintf("%d",$annualList[$i]['ANNUAL']).$setGradeName.")";
                $header5[] = "部活動".$annualName;
                $header5[] = "委員会活動".$annualName;
            }
        }
        if ($this->field['CSV_OPT3']) {
            for ($i=0; $i < get_count($annualList); $i++) {
                $annualName = "(".sprintf("%d",$annualList[$i]['ANNUAL']).$setGradeName.")";
                $header5[] = "大会記録".$annualName;
            }
        }
        if ($this->field['CSV_OPT4']) {
            for ($i=0; $i < get_count($annualList); $i++) {
                $annualName = "(".sprintf("%d",$annualList[$i]['ANNUAL']).$setGradeName.")";
                $header5[] = "取得資格".$annualName;
            }
        }
        if ($this->field['CSV_OPT5']) {
            for ($i=0; $i < get_count($annualList); $i++) {
                $annualName = "(".sprintf("%d",$annualList[$i]['ANNUAL']).$setGradeName.")";
                $header5[] = "賞罰".$annualName;
            }
        }

        //ヘッダ出力
        $contents  = implode($header, ",") .",";
        if ($this->field["OUT_DIV"] == '1') {
            if (get_count($header3) > 0) $contents .= implode($header3, ",") .",";
            if (get_count($header4) > 0) $contents .= implode($header4, ",") .",";
            if (get_count($header2) > 0) $contents .= implode($header2, ",") .",";
        } else {
            if (get_count($header2) > 0) $contents .= implode($header2, ",") .",";
            if (get_count($header4) > 0) $contents .= implode($header4, ",") .",";
            if (get_count($header3) > 0) $contents .= implode($header3, ",") .",";
        }
        if (get_count($header5) > 0) {
            $contents .= implode($header5, ",") .",";
        }

        $contents .= "\n";

        //性別
        $query = knje131tQuery::getSexName();
        $result = $db->query($query);
        $sex_name = array();
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $sex_name[$row["NAMECD2"]] = $row["ABBV1"];
        }

        //SQL文発行
        $query = knje131tQuery::getSelectCsv($this);
        $result = $db->query($query);
        $data = false;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row["SEX"] = $sex_name[$row["SEX"]];
            //頭に0がある場合は'0'をカット
            foreach($row as $key => $val) {
                if (preg_match('/VALUATION/', $key) && $val != "") {
                    $val = ltrim($val, '0');
                    $row[$key] = $val;
                }
            }
            $contents .= CTRL_YEAR . ",";
            $contents .= implode($row, ",");

            // CSV_OPT の項目取得
            if ($this->field['CSV_OPT1']) {
                for ($i=0; $i < get_count($annualList); $i++) {
                    $values = array();
                    $year = $annualList[$i]['YEAR'];
                    if ($this->field["OUT_DIV"] == '1') {
                        // 通知表
                        $query = knje131tQuery::getAttendSemes($year, $row["SCHREGNO"]);
                    } else {
                        // 調査書・要録
                        $query = knje131tQuery::getSchregAttendDat($year, $row["SCHREGNO"]);
                    }
                    $rowOpt = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    $contents .= ",".$rowOpt['SICK'];
                    $contents .= ",".$rowOpt['SUSPEND'];
                    $contents .= ",".$rowOpt['LATE_EARLY'];
                    // 総欠課時数
                    $query = knje131tQuery::getAttendSubclassDat($this, $year, $row["SCHREGNO"]);
                    $rowOpt = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    $contents .= ",".$rowOpt['NOTICE_LATE'];
                }
            }
            if ($this->field['CSV_OPT2']) {
                for ($i=0; $i < get_count($annualList); $i++) {
                    $year = $annualList[$i]['YEAR'];
                    //クラブ活動
                    $values = array();
                    $query = knje131tQuery::getClub($this, $year, $row["SCHREGNO"]);
                    $resOpt = $db->query($query);
                    while($rowOpt = $resOpt->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $value = str_replace("\n", "↓", str_replace("\r", "", str_replace(",", ".", $rowOpt['CLUB_SHOW'])));
                        $value .= "、".str_replace("\n", "↓", str_replace("\r", "", str_replace(",", ".", $rowOpt['EXECUTIVE_SHOW'])));
                        $value .= "、".str_replace("\n", "↓", str_replace("\r", "", str_replace(",", ".", $rowOpt['SDATE_TO_EDATE'])));
                        $value .= "、".str_replace("\n", "↓", str_replace("\r", "", str_replace(",", ".", $rowOpt['REMARK'])));
                        $values[] = $value;
                    }
                    $resOpt->free();
                    $contents .= ",".implode($values, "／");

                    //委員会活動
                    $values = array();
                    $query = knje131tQuery::getCommittee($this, $year, $row["SCHREGNO"]);
                    $resOpt = $db->query($query);
                    while($rowOpt = $resOpt->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $value = str_replace("\n", "↓", str_replace("\r", "", str_replace(",", ".", $rowOpt['GRADE_NAME1'])));
                        $value .= "、".str_replace("\n", "↓", str_replace("\r", "", str_replace(",", ".", $rowOpt['SEMESTER_SHOW'])));
                        $value .= "、".str_replace("\n", "↓", str_replace("\r", "", str_replace(",", ".", $rowOpt['COMMITTEE_SHOW'])));
                        $value .= "、".str_replace("\n", "↓", str_replace("\r", "", str_replace(",", ".", $rowOpt['CHARGE_SHOW'])));
                        $value .= "、".str_replace("\n", "↓", str_replace("\r", "", str_replace(",", ".", $rowOpt['EXECUTIVE_SHOW'])));
                        $values[] = $value;
                    }
                    $resOpt->free();
                    $contents .= ",".implode($values, "／");
                }
            }
            if ($this->field['CSV_OPT3']) {
                for ($i=0; $i < get_count($annualList); $i++) {
                    $values = array();
                    $year = $annualList[$i]['YEAR'];
                    $query = knje131tQuery::getSchregClubHdetailDat($this, $year, $row["SCHREGNO"]);
                    $resOpt = $db->query($query);
                    while($rowOpt = $resOpt->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $value = str_replace("\n", "↓", str_replace("\r", "", str_replace(",", ".", $rowOpt['CLUB_SHOW'])));
                        $value .= "、".str_replace("\n", "↓", str_replace("\r", "", str_replace(",", ".", $rowOpt['HOSTNAME'])));
                        $value .= "、".str_replace("\n", "↓", str_replace("\r", "", str_replace(",", ".", $rowOpt['DETAIL_DATE'])));
                        $value .= "、".str_replace("\n", "↓", str_replace("\r", "", str_replace(",", ".", $rowOpt['DIV_NAME'])));
                        $value .= "、".str_replace("\n", "↓", str_replace("\r", "", str_replace(",", ".", $rowOpt['MEET_SHOW'])));
                        $value .= "、".str_replace("\n", "↓", str_replace("\r", "", str_replace(",", ".", $rowOpt['DETAIL_REMARK'])));
                        $values[] = $value;
                    }
                    $resOpt->free();
                    $contents .= ",".implode($values, "／");
                }
            }
            if ($this->field['CSV_OPT4']) {
                for ($i=0; $i < get_count($annualList); $i++) {
                    $values = array();
                    $year = $annualList[$i]['YEAR'];
                    $query = knje131tQuery::getAward($this, $db, $year, $row["SCHREGNO"]);
                    $resOpt = $db->query($query);
                    while($rowOpt = $resOpt->fetchRow(DB_FETCHMODE_ASSOC)) {
                        if ($this->Properties["useQualifiedMst"] == '1') {
                            $value = str_replace("\n", "↓", str_replace("\r", "", str_replace(",", ".", $rowOpt['QUALIFIED_SHOW'])));
                            $value .= "、".str_replace("\n", "↓", str_replace("\r", "", str_replace(",", ".", $rowOpt['QUALIFIED_ABBV'])));
                            $value .= "、".str_replace("\n", "↓", str_replace("\r", "", str_replace(",", ".", $rowOpt['RANK_SHOW'])));
                            $value .= "、".str_replace("\n", "↓", str_replace("\r", "", str_replace(",", ".", $rowOpt['REGDDATE'])));
                            $value .= "、".str_replace("\n", "↓", str_replace("\r", "", str_replace(",", ".", $rowOpt['CONDITION_DIV'])));
                            $value .= "、".str_replace("\n", "↓", str_replace("\r", "", str_replace(",", ".", $rowOpt['PROMOTER'])));
                            $values[] = $value;
                        } else {
                            $value = str_replace("\n", "↓", str_replace("\r", "", str_replace(",", ".", $rowOpt['CONTENTS_SHOW'])));
                            $value .= "、".str_replace("\n", "↓", str_replace("\r", "", str_replace(",", ".", $rowOpt['REMARK_SHOW'])));
                            $value .= "、".str_replace("\n", "↓", str_replace("\r", "", str_replace(",", ".", $rowOpt['REGDDATE'])));
                            $value .= "、".str_replace("\n", "↓", str_replace("\r", "", str_replace(",", ".", $rowOpt['CONDITION_DIV'])));
                            $values[] = $value;
                        }
                    }
                    $resOpt->free();
                    $contents .= ",".implode($values, "／");
                }
            }
            if ($this->field['CSV_OPT5']) {
                for ($i=0; $i < get_count($annualList); $i++) {
                    $values = array();
                    $year = $annualList[$i]['YEAR'];
                    $query = knje131tQuery::getHyosyo($year, $row["SCHREGNO"]);
                    $resOpt = $db->query($query);
                    while($rowOpt = $resOpt->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $value = str_replace("\n", "↓", str_replace("\r", "", str_replace(",", ".", $rowOpt['DETAILCDNAME'])));
                        $value .= "、".str_replace("\n", "↓", str_replace("\r", "", str_replace(",", ".", $rowOpt['CONTENT_SHOW'])));
                        $value .= "、".str_replace("\n", "↓", str_replace("\r", "", str_replace(",", ".", $rowOpt['DETAIL_SDATE'])));
                        $values[] = $value;
                    }
                    $resOpt->free();
                    $contents .= ",".implode($values, "／");
                }
            }

            $contents .= "\n";
            $data = true;
        }

        $result->free();
        Query::dbCheckIn($db);

        if ($data){
            $outName = ($this->field["OUT_DIV"] == "1") ? "学年評価" : "評定";
            $couse = $outName ."平均値一覧表";
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
