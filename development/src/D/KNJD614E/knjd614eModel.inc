<?php

require_once('for_php7.php');
class knjd614eModel extends Model {
    var $semester;
    var $grade;
    var $schoolKind;
    var $cmd;
    var $control;        //コントロールマスタ

    function knjd614eModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
    }

    function init() {
        $this->schoolKind = VARS::post("SCHOOL_KIND");
        $this->grade      = VARS::post("GRADE");
        $this->cmd        = VARS::request("cmd");

        if ($this->cmd == '') {
            unset($this->semester);
            unset($this->grade);
            //unset($this->test_cd);
        }

        $this->field = array("SEMESTER"                    =>  VARS::post("SEMESTER"),                     //学期
                             "SDATE"                       =>  VARS::post("SDATE"),                        //出欠集計日付(開始日)
                             "EDATE"                       =>  VARS::post("EDATE"),                        //出欠集計日付(終了日)
                             "CREDIT_LINE"                 =>  VARS::post("CREDIT_LINE")                   //単位未修得基準
                             );
        if (!$this->field["SEMESTER"]) {
            $this->field["SEMESTER"] = CTRL_SEMESTER;
        }

        if (!$this->semester) {
            $this->semester = CTRL_SEMESTER;
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    /**
     * CSV出力
     */
    function getCsvOutputModel() {

        //DB接続
        $db = Query::dbCheckOut();

        $gradeCd = $db->getOne(knjd614eQuery::getGradeInfo($this, "GRADE_CD"));
        $print63KaikinFlg = $gradeCd == "03" ? true : false;
        $contents  = "";
        $header_data  = false;

        $contents1 = "";
        $contents2 = "";
        $contents3 = "";
        $contents4 = "";
        $contents5 = "";

        //各種データを取っていく。
        $getAry1 = array();
        $getAry2 = array();
        $getAry2_1 = array();  //コース/クラスコードのキーテーブル
        $getAry2G = array();  //学年計
        $getAry3 = array();
        $getAry4 = array();
        $getAry5 = array();
        $getAry6 = array();
        $getAry7 = array();

        //コース名称マスタを保持する。
        $ccmAry = array();
        $query = knjd614eQuery::getCourseCodeMst();
        $retObj = $db->query($query);
        while ($row = $retObj->fetchRow(DB_FETCHMODE_ASSOC)) {
            $ccmAry[$row["COURSECODE"]] = $row;
        }
        $retObj->free();

        //クラス名称マスタを保持する。
        $hrClsMstAry = array();
        $query = knjd614eQuery::getHrClsDat($this);
        $retObj = $db->query($query);
        while ($row = $retObj->fetchRow(DB_FETCHMODE_ASSOC)) {
            $hrClsMstAry[$row["HR_CLASS"]] = $row;
        }
        $retObj->free();

        ////授業日数
        $query = knjd614eQuery::getJyugyouNissuu($this);
        $retObj = $db->query($query);
        while ($row = $retObj->fetchRow(DB_FETCHMODE_ASSOC)) {
            $getAry1[] = $row;
        }
        $retObj->free();

        ////在籍者数/休学者数/復学者数/留学者数を取得
        $query = knjd614eQuery::getSchCntInfo($this);
        $retObj = $db->query($query);
        while ($row = $retObj->fetchRow(DB_FETCHMODE_ASSOC)) {
            $getAry2[] = $row;
            $std_key1 = $row["COURSECD"].$row["MAJORCD"].$row["COURSECODE"];
            $std_key2 = $row["HR_CLASS"];
            if (!is_array($getAry2_1[$std_key1])) {
                $getAry2_1[$std_key1] = array();
            }
            if (!is_array($getAry2_1[$std_key1][$std_key2])) {
                $getAry2_1[$std_key1][$std_key2] = $hrClsMstAry[$std_key2];
            }
        }
        $retObj->free();

        ////学年計
        $query = knjd614eQuery::getSchCntInfo($this, "GRADE_SUM");
        $retObj = $db->query($query);
        while ($row = $retObj->fetchRow(DB_FETCHMODE_ASSOC)) {
            $getAry2G = $row;  //学年集計結果なので、1レコードしか取れないはず。
        }
        $retObj->free();

        ////各種出席情報(出席日数不足者判定も含む)を取得
        $query = knjd614eQuery::getNotEnoughAttend($this);
        $retObj = $db->query($query);
        $fusokuFulCnt = 0;
        while ($row = $retObj->fetchRow(DB_FETCHMODE_ASSOC)) {
            $getAry3[] = $row;
            if ($row["FUSOKU_FLG"] != 0) {
                $fusokuFulCnt++;
            }
        }
        $retObj->free();

        $scoreFuryouFulCnt_1 = "";
        $scoreFuryouFulCnt_2 = "";
        if ($this->schoolKind != "J") {
            ////成績不良者を取得
            $scoreFuryouFulCnt_1 = 0;
            $scoreFuryouFulCnt_2 = 0;
            
            $query = knjd614eQuery::notYetStudent($this, false);
            $retObj = $db->query($query);
            while ($row = $retObj->fetchRow(DB_FETCHMODE_ASSOC)) {
                $getAry4[] = $row;
                if ($row["PAT1"] != "") {
                    $scoreFuryouFulCnt_1 += $row["PAT1"];
                }
                if ($row["PAT2"] != "") {
                    $scoreFuryouFulCnt_2 += $row["PAT2"];
                }
            }
            $retObj->free();

            $query = knjd614eQuery::notYetStudent($this, true);
            $retObj = $db->query($query);
            while ($row = $retObj->fetchRow(DB_FETCHMODE_ASSOC)) {
                $getAry5[] = $row;
            }
            $retObj->free();
        }

        ////成績優秀者を取得(在籍者数の5%)
        $query = knjd614eQuery::getExStudent($this);
        $retObj = $db->query($query);
        while ($row = $retObj->fetchRow(DB_FETCHMODE_ASSOC)) {
            $getAry6[] = $row;
        }
        $retObj->free();

        ////皆勤・精勤者を取得
        $k6FullCnt = 0;
        $k3FullCnt = 0;
        $k1FullCnt = 0;
        $s1FullCnt = 0;
        $query = knjd614eQuery::getKaikinSeikin($this);
        $retObj = $db->query($query);
        while ($row = $retObj->fetchRow(DB_FETCHMODE_ASSOC)) {
            $getAry7[] = $row;
            if ($print63KaikinFlg) {
                if ($row["KAIKIN_DIV"] == "1" && $row["REF_YEAR"] == "6") {
                    $k6FullCnt++;
                }
                if ($row["KAIKIN_DIV"] == "1" && $row["REF_YEAR"] == "3") {
                    $k3FullCnt++;
                }
            }
            if ($row["KAIKIN_DIV"] == "1" && $row["REF_YEAR"] == "1") {
                $k1FullCnt++;
            }
            if ($row["KAIKIN_DIV"] == "2" && $row["REF_YEAR"] == "1") {
                $s1FullCnt++;
            }
        }
        $retObj->free();

        //1.授業日数
        $contents1 = "1.授業日数\n  ".($getAry1[0]["LESSON"] == "" ? "\n" : $getAry1[0]["LESSON"]."日\n");
        //2.各種資料該当者一覧表
        $contents2 = "2.各種資料該当者一覧表\n";
        foreach ($getAry2_1 as $courseKey => $subAry) {
            if (strlen($courseKey) < 8) {
                continue;
            }
            $contents2 .= "コース:".$ccmAry[substr($courseKey, 4)]["COURSECODENAME"]."\n";
            $header_data = true;   //出力フラグ
            $contents2 .= "クラス";
            $colCnt = 0;
            foreach ($subAry as $hrKey => $hrName) {
                $contents2 .= ",".intval($hrKey);
                $colCnt++;
            }
            if ($colCnt < 10) {
                for ($cCnt = $colCnt;$cCnt < 10;$cCnt++) {
                    $contents2 .= ",";
                }
            }
            $contents2 .= ",計,学年計";
            $contents2 .= "\n";

            $contents2_1 = "在籍者数";
            $contents2_2 = "休学者数";
            $contents2_3 = "復学者数";
            $contents2_4 = "留学者数";
            $sum2_1 = 0;
            $sum2_2 = 0;
            $sum2_3 = 0;
            $sum2_4 = 0;
            foreach ($subAry as $hrKey => $hrName) {
                $print2Flg = false;
                foreach ($getAry2 as $detData) {
                    if ($detData["COURSECD"].$detData["MAJORCD"].$detData["COURSECODE"] == $courseKey && $detData["HR_CLASS"] == $hrKey) {
                        $contents2_1 .= ",".$detData["ZAISEKI_CNT"];
                        $sum2_1 += $detData["ZAISEKI_CNT"];
                        $contents2_2 .= ",".$detData["KYUUGAKU_CNT"];
                        $sum2_2 += $detData["KYUUGAKU_CNT"];
                        $contents2_3 .= ",".$detData["FUKUGAKU_CNT"];
                        $sum2_3 += $detData["FUKUGAKU_CNT"];
                        $contents2_4 .= ",".$detData["RYUUGAKU_CNT"];
                        $sum2_4 += $detData["RYUUGAKU_CNT"];
                        $print2Flg = true;
                        break;
                    }
                }
                if (!$print2Flg) {
                    $contents2_1 .= ",";
                    $contents2_2 .= ",";
                    $contents2_3 .= ",";
                    $contents2_4 .= ",";
                }
            }
            if (get_count($subAry) < 10) {
                for ($cCnt = 0;$cCnt < 10 -get_count($subAry);$cCnt++) {
                    $contents2_1 .= ",";
                    $contents2_2 .= ",";
                    $contents2_3 .= ",";
                    $contents2_4 .= ",";
                }
            }
            //合計
            $contents2_1 .= ",".$sum2_1.",".$getAry2G["ZAISEKI_CNT"];
            $contents2_2 .= ",".$sum2_2.",".$getAry2G["KYUUGAKU_CNT"];
            $contents2_3 .= ",".$sum2_3.",".$getAry2G["FUKUGAKU_CNT"];
            $contents2_4 .= ",".$sum2_4.",".$getAry2G["RYUUGAKU_CNT"];

            $contents2 .= $contents2_1."\n";
            $contents2 .= $contents2_2."\n";
            $contents2 .= $contents2_3."\n";
            $contents2 .= $contents2_4."\n";

            $contents2_5 = "出席日数不足者数";
            $sum2_5 = 0;
            foreach ($subAry as $hrKey => $hrName) {
                $hCnt = 0;
                foreach ($getAry3 as $detData) {
                    if ($detData["COURSECD"].$detData["MAJORCD"].$detData["COURSECODE"] == $courseKey && $detData["HR_CLASS"] == $hrKey) {
                        if ($detData["FUSOKU_FLG"] != 0) {
                            $hCnt++;
                        }
                    }
                }
                $contents2_5 .= ",".$hCnt;
                $sum2_5 += $hCnt;
            }
            if (get_count($subAry) < 10) {
                for ($cCnt = 0;$cCnt < 10 -get_count($subAry);$cCnt++) {
                    $contents2_5 .= ",";
                }
            }
            //合計
            $contents2_5 .= ",".$sum2_5.",".$fusokuFulCnt;
            $contents2 .= $contents2_5."\n";

            $contents2_6_1 = "成績不良者数(基準以内)";
            $contents2_6_2 = "成績不良者数(基準以上)";
            $contents2_6_3 = "成績不良者数(計)";
            $sum2_6_1 = 0;
            $sum2_6_2 = 0;
            $sum2_6_3 = 0;
            foreach ($subAry as $hrKey => $hrName) {
                $print4Flg = false;
                foreach($getAry4 as $detData) {
                    if ($detData["COURSECD"].$detData["MAJORCD"].$detData["COURSECODE"] == $courseKey && $detData["HR_CLASS"] == $hrKey) {
                        $contents2_6_1 .= ",".$detData["PAT1"];
                        $sum2_6_1 += $detData["PAT1"];
                        $contents2_6_2 .= ",".$detData["PAT2"];
                        $sum2_6_2 += $detData["PAT2"];
                        $contents2_6_3 .= ",".($detData["PAT1"] + $detData["PAT2"]);
                        $sum2_6_3 += ($detData["PAT1"] + $detData["PAT2"]);
                        $print4Flg = true;
                    }
                }
                if (!$print4Flg) {
                    $contents2_6_1 .= ",";
                    $contents2_6_2 .= ",";
                    $contents2_6_3 .= ",";
                }
            }
            if (get_count($subAry) < 10) {
                for ($cCnt = 0;$cCnt < 10 -get_count($subAry);$cCnt++) {
                    $contents2_6_1 .= ",";
                    $contents2_6_2 .= ",";
                    $contents2_6_3 .= ",";
                }
            }
            //合計
            $contents2_6_1 .= ",".$sum2_6_1.",".$scoreFuryouFulCnt_1;
            $contents2_6_2 .= ",".$sum2_6_2.",".$scoreFuryouFulCnt_2;
            $contents2_6_3 .= ",".$sum2_6_3.",".($scoreFuryouFulCnt_1 + $scoreFuryouFulCnt_2);

            $contents2 .= $contents2_6_1."\n";
            $contents2 .= $contents2_6_2."\n";
            $contents2 .= $contents2_6_3."\n";

            $contents2_7 = "素行不良者";
            foreach ($subAry as $hrKey => $hrName) {
                    $contents2_7 .= ",";  //空で指定。
            }
            if (get_count($subAry) < 10) {
                for ($cCnt = 0;$cCnt < 10 -get_count($subAry);$cCnt++) {
                    $contents2_7 .= ",";
                }
            }
            //合計も空で指定。
            $contents2_7 .= ",,";  //空で指定。
            $contents2 .= $contents2_7."\n";

            $contents2_8 = "成績優良者";
            $sum2_8 = 0;
            foreach ($subAry as $hrKey => $hrName) {
                $hCnt = 0;
                foreach ($getAry6 as $detData) {
                    if ($detData["COURSECD"].$detData["MAJORCD"].$detData["COURSECODE"] == $courseKey && $detData["HR_CLASS"] == $hrKey) {
                        $hCnt++;
                    }
                }
                $contents2_8 .= ",".$hCnt;
                $sum2_8 += $hCnt;
            }
            if (get_count($subAry) < 10) {
                for ($cCnt = 0;$cCnt < 10 -get_count($subAry);$cCnt++) {
                    $contents2_8 .= ",";
                }
            }
            //合計
            $contents2_8 .= ",".$sum2_8.",".get_count($getAry6);
            $contents2 .= $contents2_8."\n";

            $contents2_9_1 = "皆勤者(6ヵ年)";
            $contents2_9_2 = "皆勤者(3ヵ年)";
            $contents2_9_3 = "皆勤者(1ヵ年)";
            $contents2_9_4 = "精勤者";
            $sum2_9_1 = 0;
            $sum2_9_2 = 0;
            $sum2_9_3 = 0;
            $sum2_9_4 = 0;
            foreach ($subAry as $hrKey => $hrName) {
                $hCnt_1 = 0;
                $hCnt_2 = 0;
                $hCnt_3 = 0;
                $hCnt_4 = 0;
                foreach ($getAry7 as $detData) {
                    if ($detData["COURSECD"].$detData["MAJORCD"].$detData["COURSECODE"] == $courseKey && $detData["HR_CLASS"] == $hrKey) {
                        if ($print63KaikinFlg) {
                            if ($detData["KAIKIN_DIV"] == "1" && $detData["REF_YEAR"] == "6") {
                                $hCnt_1++;
                            }
                            if ($detData["KAIKIN_DIV"] == "1" && $detData["REF_YEAR"] == "3") {
                                $hCnt_2++;
                            }
                        }
                        if ($detData["KAIKIN_DIV"] == "1" && $detData["REF_YEAR"] == "1") {
                            $hCnt_3++;
                        }
                        if ($detData["KAIKIN_DIV"] == "2" && $detData["REF_YEAR"] == "1") {
                            $hCnt_4++;
                        }
                    }
                }
                if ($print63KaikinFlg) {
                    $contents2_9_1 .= ",".$hCnt_1;
                    $contents2_9_2 .= ",".$hCnt_2;
                } else {
                    $contents2_9_1 .= ",";
                    $contents2_9_2 .= ",";
                }
                $contents2_9_3 .= ",".$hCnt_3;
                $contents2_9_4 .= ",".$hCnt_4;
                $sum2_9_1 += $hCnt_1;
                $sum2_9_2 += $hCnt_2;
                $sum2_9_3 += $hCnt_3;
                $sum2_9_4 += $hCnt_4;
            }
            if (get_count($subAry) < 10) {
                for ($cCnt = 0;$cCnt < 10 -get_count($subAry);$cCnt++) {
                    $contents2_9_1 .= ",";
                    $contents2_9_2 .= ",";
                    $contents2_9_3 .= ",";
                    $contents2_9_4 .= ",";
                }
            }
            //合計
            if ($print63KaikinFlg) {
                $contents2_9_1 .= ",".$sum2_9_1.",".$k6FullCnt;
                $contents2_9_2 .= ",".$sum2_9_2.",".$k3FullCnt;
            } else {
                $contents2_9_1 .= ",,";
                $contents2_9_2 .= ",,";
            }
            $contents2_9_3 .= ",".$sum2_9_3.",".$k1FullCnt;
            $contents2_9_4 .= ",".$sum2_9_4.",".$s1FullCnt;
            $contents2 .= $contents2_9_1."\n";
            $contents2 .= $contents2_9_2."\n";
            $contents2 .= $contents2_9_3."\n";
            $contents2 .= $contents2_9_4."\n";
        }
        $contents2 .= "\n";


        //出席日数不足者
        $contents3  = "3.出席日数不足者\n";
        $contents3 .= "組,番,氏名,欠席,遅刻,早退,換算合計\n";
        foreach ($getAry3 as $detData) {
            if ($detData["FUSOKU_FLG"] != 0) {
                $contents3 .= $detData["HR_NAMEABBV"].",".$detData["ATTENDNO"].",".$detData["NAME"].",".$detData["ABSENT"].",".$detData["LATE"].",".$detData["EARLY"].",".($detData["ABSENT"] + $detData["KANSAN_VAL"])."\n";
            }
        }
        $contents3 .="\n";

        //成績優良者
        $contents4 = "4.成績優良者\n";

        foreach ($getAry2_1 as $courseKey => $subAry) {
            if (strlen($courseKey) < 8) continue;
            $contents4 .= "コース:".$ccmAry[substr($courseKey, 4)]["COURSECODENAME"]."\n";
            $contents4 .= "組,番,氏名,合計,平均,欠席,遅刻,早退\n";
            $prtFlg = false;
            foreach ($getAry6 as $detData) {
                if ($detData["COURSECD"].$detData["MAJORCD"].$detData["COURSECODE"] == $courseKey) {
                    $attStr = ",,,";
                    foreach($getAry3 as $fData) {
                        if ($fData["SCHREGNO"] == $detData["SCHREGNO"]) {
                            $attStr = ",".$fData["ABSENT"].",".$fData["LATE"].",".$fData["EARLY"];
                            break;
                        }
                    }
                    $hrInfo = array();
                    $hrInfo = $hrClsMstAry[$detData["HR_CLASS"]];
                    $contents4 .= $hrInfo["HR_NAMEABBV"].",".$detData["ATTENDNO"].",".$detData["NAME"].",".$detData["SCORE"].",".$detData["AVG"].$attStr."\n";
                    $prtFlg = true;
                }
            }
            if (!$prtFlg) {
                $contents4 .= "\n";
            }
            $contents4 .= "\n";
        }

        //単位未修得者
        $contents5 = "";
        if ($this->schoolKind != "J") {
            $contents5 = "5.単位未修得者\n";
            $c5Header = "組,番,氏名,合計,平均,欠席,遅刻,早退\n";

            foreach ($getAry2_1 as $courseKey => $subAry) {
                if (strlen($courseKey) < 8) continue;
                $contents5_1 = "修得単位数 ".$this->field["CREDIT_LINE"]."以上 コース:".$ccmAry[substr($courseKey, 4)]["COURSECODENAME"]."\n";
                $contents5_1 .= $c5Header;
                $contents5_2 = "修得単位数 ".$this->field["CREDIT_LINE"]."未満 コース:".$ccmAry[substr($courseKey, 4)]["COURSECODENAME"]."\n";
                $contents5_2 .= $c5Header;
                $prt1Flg = false;
                $prt2Flg = false;
                foreach ($getAry5 as $detData) {
                    if ($detData["COURSECD"].$detData["MAJORCD"].$detData["COURSECODE"] == $courseKey) {
                        $attStr = ",,,";
                        foreach($getAry3 as $fData) {
                            if ($fData["SCHREGNO"] == $detData["SCHREGNO"]) {
                                $attStr = ",".$fData["ABSENT"].",".$fData["LATE"].",".$fData["EARLY"];
                                break;
                            }
                        }
                        $hrInfo = array();
                        $hrInfo = $hrClsMstAry[$detData["HR_CLASS"]];
                        if ($detData["PAT1"] == "1") {
                            $contents5_1 .= $hrInfo["HR_NAMEABBV"].",".$detData["ATTENDNO"].",".$detData["NAME"].",".$detData["SCORE"].",".$detData["AVG"].$attStr."\n";
                            $prt1Flg = true;
                        }
                        if ($detData["PAT2"] == "1") {
                            $contents5_2 .= $hrInfo["HR_NAMEABBV"].",".$detData["ATTENDNO"].",".$detData["NAME"].",".$detData["SCORE"].",".$detData["AVG"].$attStr."\n";
                            $prt2Flg = true;
                        }
                    }
                }
                if (!$prt1Flg) {
                    $contents5_1 .= "\n";
                }
                if (!$prt2Flg) {
                    $contents5_2 .= "\n";
                }
                $contents5 .= $contents5_1;
                $contents5 .= "\n";
                $contents5 .= $contents5_2;
                $contents5 .= "\n";
            }
        }

        //皆勤賞受賞候補者
        $printKaikinTypeAry = array();
        $query = knjd614eQuery::getKaikinType($this);
        $retObj = $db->query($query);
        while ($row = $retObj->fetchRow(DB_FETCHMODE_ASSOC)) {
            $printKaikinTypeAry[] = $row;
        }
        $retObj->free();

        $contents6 = "";

        foreach ($printKaikinTypeAry as $kTypDat) {
            //中3,高3以外で1ヵ年以外なら出力対象外
            if (!$print63KaikinFlg && $kTypDat["REF_YEAR"] != "1") {
                continue;
            }
            $contents6_1 = "";

            $line1PrtMax = 4;
            $prtSchCnt = 0;
            $prtSchSubCnt = 0;
            $print1Line = "";
            $sep = "";
            foreach ($getAry7 as $detData) {
                if ($detData["KAIKIN_CD"] == $kTypDat["KAIKIN_CD"]) {
                    $print1Line .= $sep.$detData["HR_NAMEABBV"].",".$detData["NAME"];
                    $sep = ",";
                    $prtSchCnt++;
                    $prtSchSubCnt++;
                    if ($prtSchSubCnt >= $line1PrtMax) {
                        $contents6_1 .= $print1Line."\n";
                        $sep = "";
                        $prtSchSubCnt = 0;
                    }
                }
            }
            if ($prtSchSubCnt != 0) {
                for ($cCnt = $prtSchSubCnt;$cCnt < $line1PrtMax;$cCnt++) {
                    $print1Line .= ",";
                }
                $contents6_1 .= $print1Line."\n";
                $sep = "";
                $prtSchSubCnt = 0;
            }
            //このタイミングで出力人員が決まるので、ここで出力
            $c6Header = CTRL_YEAR."年度 ".($kTypDat["KAIKIN_DIV"] == "2" ? "精勤" : "皆勤")."賞受賞候補者\n";
            $c6Header .= $db->getOne(knjd614eQuery::getGradeInfo($this, "GRADE_NAME2"))."        ".$kName."        以下".$prtSchCnt."名\n";
            $c6Header .= "組,氏名".str_repeat(",組,氏名", $line1PrtMax - 1)."\n";
            
            $contents6 .= $c6Header;
            $contents6 .= $contents6_1."\n";
        }

        $contents = "";
        $contents .= $contents1;
        $contents .= $contents2;
        $contents .= $contents3;
        $contents .= $contents4;
        $contents .= $contents5;
        $contents .= $contents6;

        Query::dbCheckIn($db);

        //CSV出力
        if ($header_data) {
            $couse = "収入-支出細目一覧データ";

            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    /**
     * データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
     */
    function commaConv($row) {
        $row = str_replace(","   , "." , $row);
        $row = str_replace("\r\n", "↓", $row);
        $row = str_replace("\r"  , "↓", $row);
        $row = str_replace("\n"  , "↓", $row);

        return $row;
    }
}
?>
