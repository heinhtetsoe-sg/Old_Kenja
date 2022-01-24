<?php

require_once('for_php7.php');

class knjj143Model extends Model {
    var $cmd;
    var $control;

    function knjj143Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->cmd = VARS::request("cmd");

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

    }

    function &downloadCsvFile($csv_pattern) {
        //DB接続
        $db = Query::dbCheckOut();

        $chkcnt = $this->chkCsvData($db, $csv_pattern);
        if ($chkcnt > 0){
            $contents = $this->makeCsvData($db, $csv_pattern);
            if ($contents !== ""){
                $fsubname = preg_split("/,/", $contents, 2);
                $filename = $fsubname[0] .".csv";
                common::downloadContents($contents, $filename, "SJIS-win");
            }else{
                $this->setMessage("MSG310");
            }
        }else{
            $this->setMessage("MSG303");
        }

        //DB切断
        Query::dbCheckIn($db);
    }

    function chkCsvData($db, $csv_pattern) {
        $result = null;
        if ("csv_council" === $csv_pattern) {
            $query = knjj143Query::getMaxCouncilTypeCnt($this);
        } else if ("csv_committee" === $csv_pattern) {
            $query = knjj143Query::getMaxCommitteeTypeCnt($this);
        }
        $result = $db->getOne($query);
        if (!is_null($result)) {
            return 1;
        }
        return 0;
    }
        

    function makeCsvData($db, $csv_pattern) {
        $retval = "";

        //タイトル,日付
        $changeYear = CTRL_YEAR;
        $eyear = common::Calc_Wareki($changeYear, "4", "1");
        $retval .= $eyear . "度" . $testdivstr . "内部選考資料,日付:" . CTRL_DATE . "\r\n";
        //項目名称(先頭のみ)
        $retval .= "校種";

        //csv出力パターン毎に処理を実施
        if ("csv_council" == $csv_pattern) {
            $retval = $this->makeCouncilData($db, $csv_pattern, $retval);
        } else if ("csv_committee" == $csv_pattern) {
            $retval .= ",年組名称";
            $retval = $this->makeCommitteeData($db, $csv_pattern, $retval);
        }
        return $retval;
    }

    function makeCouncilData($db, $csv_pattern, $retval) {
        //データを取得して、タイトルの列数を設定して後、データを設定

        //取り扱う校種を確定する。
        $fixSchoolKind = $this->fixSchoolKindStr();
        //最大挿入される件数を取得する
        $query = knjj143Query::getMaxCouncilTypeCnt($this);
        $result = $db->query($query);
        $council_all_type = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $council_all_type[] = $row;
        }

        //生徒会のデータを取得(中学)
        $council_j_lst = array();
        if (strpos($fixSchoolKind, "J") !== false) {
            $query = knjj143Query::getCouncilInfo($this, "J");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $council_j_lst[] = $row;
            }
        }

        //生徒会のデータを取得する(高校)
        $council_h_lst = array();
        if (strpos($fixSchoolKind, "H") !== false) {
            $query = knjj143Query::getCouncilInfo($this, "H");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $council_h_lst[] = $row;
            }
        }

        //タイトル作成
        $council_all_lst = array();
        foreach ($council_all_type as $val) {
            for ($idx = 0;$idx < $val['WRITE_MAX'];$idx++) {
                $retval .= ",".$val['POSITION_NM'].",,";
                $council_all_lst[] = $val;
            }
        }
        $retval .= "\r\n";

        //中学生データを出力
        $retval = $this->setCouncilData($db, $retval, $council_all_lst, $council_j_lst, "J", $fixSchoolKind);

        //高校生データを出力
        $retval = $this->setCouncilData($db, $retval, $council_all_lst, $council_h_lst, "H", $fixSchoolKind);

        return $retval;
    }

    function setCouncilData($db, $retval, $council_all_lst, $council_chk_lst, $schstr, $fixSchoolKind) {
        if (strpos($fixSchoolKind, $schstr) !== false) {
            //先頭の校種の文字列を取得
            $query = knjj143Query::getSchkindStr($schstr);
            $result = $db->getOne($query);
            $retval .= $result;

            //データ設定
            $council_chk_cnt = 0;
            foreach ($council_all_lst as $val) {
                if ($council_chk_cnt < get_count($council_chk_lst) && $val['EXECUTIVECD'] === $council_chk_lst[$council_chk_cnt]['EXECUTIVECD']) {
                    $retval .= ",".$council_chk_lst[$council_chk_cnt]['NAME'].",".$council_chk_lst[$council_chk_cnt]['NAME_KANA'].",".$council_chk_lst[$council_chk_cnt]['HR_NAMEABBV'];
                    $council_chk_cnt++;
                } else {
                    $retval .= ",,,";
                }
            }
            $retval .= "\r\n";
        }
        return $retval;
    }

    function makeCommitteeData($db, $csv_pattern, $retval) {
        //データを取得して、タイトルの列数を設定して後、データを設定

        //取り扱う校種を確定する。
        $fixSchoolKind = $this->fixSchoolKindStr();
        //最大挿入される件数を取得する
        $query = knjj143Query::getMaxCommitteeTypeCnt($this);
        $result = $db->query($query);
        $committee_all_type = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $committee_all_type[] = $row;
        }

        //委員会のデータを取得(中学)
        $committee_j_lst = array();
        if (strpos($fixSchoolKind, "J") !== false) {
            $query = knjj143Query::getCommitteeInfo($this, "J");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $committee_j_lst[] = $row;
            }
        }

        //委員会のデータを取得する(高校)
        $committee_h_lst = array();
        if (strpos($fixSchoolKind, "H") !== false) {
            $query = knjj143Query::getCommitteeInfo($this, "H");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $committee_h_lst[] = $row;
            }
        }

        //タイトル作成
        $committee_all_lst = array();
        foreach ($committee_all_type as $val) {
            $firstoutflg = false;
            for ($idx = 0;$idx < $val['WRITE_MAX'];$idx++) {
                if (!$firstoutflg) {
                    $retval .= ",".$val['COMMITTEE_NM'].",,";
                    $firstoutflg = true;
                } else {
                    $retval .= ",,,";
                }
                $committee_all_lst[] = $val;
            }
        }
        $retval .= "\r\n";

        //中学生データを出力
        if (strpos($fixSchoolKind, "J") !== false) {
            //データ設定
            $retval = $this->setCommitteeData($db, $retval, $committee_all_lst, $committee_j_lst, "J");

            $retval .= "\r\n";
        }

        //高校生データを出力
        if (strpos($fixSchoolKind, "H") !== false) {
            //データ設定
            $retval = $this->setCommitteeData($db, $retval, $committee_all_lst, $committee_h_lst, "H");

            $retval .= "\r\n";
        }
        return $retval;
    }

    function setCommitteeData($db, $retval, $committee_all_lst, $committee_chk_lst, $schstr) {
        //先頭の校種の文字列を取得
        $query = knjj143Query::getSchkindStr($schstr);
        $result = $db->getOne($query);
        $retval .= $result.",";
        
        $beforeclass = "";
        $committee_all_cnt = 0;
        for($committee_chk_cnt = 0;$committee_chk_cnt < get_count($committee_chk_lst);$committee_chk_cnt++) {
            if ($beforeclass !== $committee_chk_lst[$committee_chk_cnt]['HR_CLASS']) {
                //次の年組なので、改行して校種を出力
                if ($beforeclass !== "") {
                    //改行時には、先頭から処理を行うため、改行前に残った列にカンマを埋める。
                    for (;$committee_all_cnt < get_count($committee_all_lst);$committee_all_cnt++) {
                        $retval .= ",,,";
                    }
                    $retval .= "\r\n";
                    $retval .= $result . ",";
                    //改行後は、先頭から処理を行うため、列カウンタをクリア
                    $committee_all_cnt = 0;
                }
                //年組を出力
                $retval .= $committee_chk_lst[$committee_chk_cnt]['HR_NAMEABBV'];
            }
            for (;$committee_all_cnt < get_count($committee_all_lst);$committee_all_cnt++) {
                if ($committee_all_lst[$committee_all_cnt]['COMMITTEE_FLG'] == $committee_chk_lst[$committee_chk_cnt]['COMMITTEE_FLG']) {
                    //データを出力
                    $retval .= ",".$committee_chk_lst[$committee_chk_cnt]['POSITION_NM'].",".$committee_chk_lst[$committee_chk_cnt]['NAME'].",".$committee_chk_lst[$committee_chk_cnt]['NAME_KANA'];
                    $committee_all_cnt++;
                    break;
                } else {
                    //空の列として出力
                    $retval .= ",,,";
                }
            }
            $beforeclass = $committee_chk_lst[$committee_chk_cnt]['HR_CLASS'];
        }
        //残った列があれば、カンマを埋める。
        for (;$committee_all_cnt < get_count($committee_all_lst);$committee_all_cnt++) {
            $retval .= ",,,";
        }
        return $retval;
    }

    function fixSchoolKindStr() {
        $retstr = "";
        if ("1" === $this->Properties["use_prg_schoolkind"]) {
            if ("" !== $model->selectSchoolKind) {
                $retstr = $this->selectSchoolKind;
            }
        } else if ("1" === $this->Properties["useSchool_KindField"] && "" !== $this->urlSchoolKind) {
            $retstr = $this->urlSchoolKind;
        }
        return $retstr;
    }

    function getSchoolKindInState($model) {
        $retStr = "";
        if ("1" !== ($model->Properties["use_prg_schoolkind"])) {
            return $retStr;
        }
        $cutwk = $model->selectSchoolKind;
        if (null === $cutwk || "" === $cutwk) {
            $cutwk = "P:J:H";
        }
        $strSplit = explode(":", $cutwk);
        $sep = "";
        for ($i = 0; $i < get_count($strSplit); $i++) {
            $retStr .= $sep . "'" . $strSplit[$i] . "'";
            $sep = ",";
        }
        return $retStr;
    }


}
?>
