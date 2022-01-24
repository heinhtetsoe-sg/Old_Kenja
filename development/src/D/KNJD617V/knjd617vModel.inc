<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd617vModel extends Model {
    var $field      = array();
    var $cmd;
    var $subclassGroup;
    var $control;        //コントロールマスタ
    var $schoolName;
    var $schoolCode;

    /**
     * メンバ変数の初期化
     */
    function knjd617vModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "SEMESTER"             =>  VARS::post("SEMESTER"),             //学期コンボ
                             "GRADE"                =>  VARS::post("GRADE"),                //学年
                             "MAJOR"                =>  VARS::post("MAJOR"),                //学科
                             "HR_CLASS"             =>  VARS::post("HR_CLASS"),             //クラス
                             "TESTCD"               =>  VARS::post("TESTCD"),               //テスト
                             "PRINT_DIV"            =>  VARS::post("PRINT_DIV"),            //出力方法
                             "GROUP_DIV"            =>  VARS::post("GROUP_DIV"),            //グループ
                             "OUTPUT_KIJUN"         =>  VARS::post("OUTPUT_KIJUN"),         //順位の基準点
                             "SORT_DIV"             =>  VARS::post("SORT_DIV"),             //出力順
                             "SUBCLASS_GROUP_DIV"   =>  VARS::post("SUBCLASS_GROUP_DIV"),   //科目区分
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        //初期値の設定
        $this->subclassGroup    = (strlen($this->Properties["useSubclassGroup"])) ? $this->Properties["useSubclassGroup"] : 0;

        //リスト選択
        $this->selectList = VARS::post("selectGradeHrClass");

        //テスト名称
        $this->testcdName = VARS::post("TESTCD_NAME");
    }

    function clean() {}

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        //クリア
        $contents = "";

        //改行コード、カンマ
        $kai  = "\n";
        $sep  = ",";
        $sep2 = ",,";

        //校種取得
        $query = knjd617vQuery::getGdat($this->field["GRADE"]);
        $schKind = $db->getOne($query);

        //定数セット
        $subCd333333 = "33-{$schKind}-99-333333";
        $subCd555555 = "55-{$schKind}-99-555555";
        $subCd999999 = "99-{$schKind}-99-999999";
        $subCd99999A = "99-{$schKind}-99-99999A";
        $subCd99999B = "99-{$schKind}-99-99999B";

        //元号セット
        $query = knjd617vQuery::getNameMstL007();
        $gengouRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $nen = CTRL_YEAR - (int)$gengouRow["NAMESPARE1"] + 1;
        $nenName = $nen == 1 ? '元': $nen;
        $setGengou = $gengouRow["NAME1"].$nenName;

        //学年名称
        $query = knjd617vQuery::getGradeName($this);
        $gradeName = $db->getOne($query);

        //学期名称
        $query = knjd617vQuery::getSemesterName($this);
        $semeName = $db->getOne($query);

        //タイトル
        $title = $setGengou."年度".$sep.$gradeName.'　'.$semeName.$sep.$this->testcdName."成績順一覧表";
        $contents .= $title.$kai;

        //クラス毎の平均等情報取得
        $avgArr = array();
        $query = knjd617vQuery::getAverageInfo($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setKey  = $row["AVG_DIV"].':'.$row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"];
            $setKey2 = $row["GRADE"].'-'.$row["HR_CLASS"];
            $avgArr[$setKey][$setKey2] = array("SCORE"      => $row["SCORE"],
                                               "HIGHSCORE"  => $row["HIGHSCORE"],
                                               "LOWSCORE"   => $row["LOWSCORE"],
                                               "AVG"        => $row["AVG"],
                                               "STDDEV"     => $row["STDDEV"]);
        }

        //初期化
        $gradeHrClassArr = $grpArr = $subclassArr = array();

        if ($this->field["PRINT_DIV"] == '2') { // 学年指定の時はダミーをセットしておく
            $gradeHrClassArr[0] = '';
        } else {
            $gradeHrClassArr = explode(',', $this->selectList);
        }

        //選択クラス毎に出力
        foreach ($gradeHrClassArr as $key => $gradeHrClass) {
            //グループ情報セット
            $grpArr = array();
            $query = knjd617vQuery::getGroup($this, $gradeHrClass);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $grpArr[$row["GROUP"]] = $row["GROUP"];
            }

            //グループ毎に出力
            foreach ($grpArr as $key => $group) {

                /**************/
                /* 上部データ */
                /**************/
                //クラス情報セット
                $hrClassArr = array();
                $query = knjd617vQuery::getHrclass($this, $group, $gradeHrClass);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $hrClassArr[$row["GRADE"].'-'.$row["HR_CLASS"]] = $row["HR_NAME"];
                }

                //表タイトル
                $contents .= $kai.$sep2."1.クラス別平均点一覧表".$kai;

                //科目取得
                $subclassArr = array();
                $query = knjd617vQuery::GetSubclass($this, $group, $gradeHrClass);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $subclassArr[$row["SUBCLASSCD"]] = $row["SUBCLASSNAME"];
                    $data = true;
                }
                $subclassArr["TOTAL"]   = '計';
                $subclassArr["AVG"]     = '平均';

                $contents .= $sep2.'クラス＼科目';
                foreach ($subclassArr as $suclassCd => $subClassName) {
                    $contents .= $sep.$subClassName;
                }
                $contents .= $kai;

                //クラスデータセット
                foreach ($hrClassArr as $keyGradeHr => $hrName) {
                    //クラス名称
                    $contents .= $sep2.$hrName.' 平均点';

                    //クラス毎平均をセット
                    $avgAvg = 0;
                    $avgCnt = 0;
                    foreach ($subclassArr as $suclassCd => $subClassName) {
                        $avg = $setAvg = 0;
                        if ($suclassCd == 'TOTAL') {
                            $avg = $avgArr['2:'.$subCd999999][$keyGradeHr]["AVG"];
                        } else if ($suclassCd == 'AVG') {
                            if ($avgCnt > 0) {
                                $avg = $avgAvg / $avgCnt;
                            }
                        } else {
                            $avg = $avgArr['2:'.$suclassCd][$keyGradeHr]["AVG"];
                            $avgAvg += $avg;
                            if ($avg != '') $avgCnt++;
                        }
                        if ($avg != '') {
                            $setAvg = sprintf('%.1f', round($avg , 2));
                        }
                        $contents .= $sep.$setAvg;
                    }
                    $contents .= $kai;
                }

                //学年データセット
                $koumokuArr = array("AVG"        => '平均点',
                                    "STDDEV"     => '標準偏差',
                                    "HIGHSCORE"  => '最高点',
                                    "LOWSCORE"   => '最低点');
                foreach ($koumokuArr as $fieldName => $label) {
                    $contents .= $sep2.$label;

                    $avgAvg = '';
                    $avgCnt = 0;
                    foreach ($subclassArr as $suclassCd => $subClassName) {
                        $setKey2 = $this->field["GRADE"].'-000';
                        $tmp = $setTmp = '';
                        if ($suclassCd == 'TOTAL' && $fieldName == 'AVG') {
                            $tmp = $avgArr['1:'.$subCd999999][$setKey2][$fieldName];
                        } else if ($suclassCd == 'AVG' && $fieldName == 'AVG') {
                            if ($avgCnt > 0) {
                                $tmp = $avgAvg / $avgCnt;
                            }
                        } else {
                            $tmp = $avgArr['1:'.$suclassCd][$setKey2][$fieldName];
                            $avgAvg += $tmp;
                            if ($tmp != '') $avgCnt++;
                        }
                        if ($tmp != '') {
                            if ($fieldName == 'AVG') {
                                $setTmp = sprintf('%.1f', round($tmp , 2));
                            } else {
                                $setTmp = $tmp;
                            }
                        }
                        $contents .= $sep.$setTmp;
                    }

                    $contents .= $kai;
                }
                $contents .= $kai;

                /**************/
                /* 下部データ */
                /**************/

                //科目タイトルセット
                $subclassArr["TOTAL"]       = '総点';
                $subclassArr["AVG"]         = '平均';
                $subclassArr["CLASS_RANK"]  = '組順';
                $subclassArr["COURSE_RANK"] = 'コース';
                if ("1" == $this->field["PRINT_DIV"]) {
                    $subclassArr["GRADE_RANK"]  = '学年順';
                    $rank = '';
                } else {
                    $rank = '順位';
                }

                $contents .= $rank.$sep.'年組番'.$sep.'氏名';
                foreach ($subclassArr as $subclassCd => $subClassName) {
                    $contents .= $sep.$subClassName;
                }
                $contents .= $kai;

                //生徒個人データ取得
                $schInfo = array();
                $query = knjd617vQuery::getStudentInfo($this, $group, $gradeHrClass);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $schInfo[$row["SCHREGNO"]]['HR_CLASS'] = $row["HR_CLASS"];
                    $schInfo[$row["SCHREGNO"]]['ATTENDNO'] = $row["ATTENDNO"];
                    $schInfo[$row["SCHREGNO"]]['INOUTCD']  = $row["INOUTCD"];
                    $schInfo[$row["SCHREGNO"]]['NAME']     = $row["NAME"];
                }
                //生徒個人データ取得（順位等）
                $query = knjd617vQuery::getRankInfo($this, $gradeHrClass);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $setSubclassKey = $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"];
                    $schInfo[$row["SCHREGNO"]][$setSubclassKey] = array('SCORE'             => $row["SCORE"],
                                                                        'TOTAL'             => $row["SCORE"],
                                                                        'AVG'               => $row["AVG"],
                                                                        'GRADE_RANK'        => $row["GRADE_RANK"],
                                                                        'CLASS_RANK'        => $row["CLASS_RANK"],
                                                                        'COURSE_RANK'       => $row["COURSE_RANK"],
                                                                        'COURSEGROUP_RANK'  => $row["COURSEGROUP_RANK"],
                                                                        'MAJOR_RANK'        => $row["MAJOR_RANK"]
                                                                        );
                }

                //生徒個人データセット
                foreach ($schInfo as $schregNo => $schInfoArr) {
                    if ("2" == $this->field["PRINT_DIV"]) {
                        //順位
                        $contents .= $schInfoArr[$subCd999999]['GRADE_RANK'];
                    }
                    //年組
                    $contents .= $sep.$schInfoArr["HR_CLASS"].'-'.$schInfoArr["ATTENDNO"];
                    //氏名
                    $inOut = $schInfoArr["INOUTCD"] == '0' ? 'M ': '';
                    $contents .= $sep.$inOut.$schInfoArr["NAME"];

                    //科目点数セット
                    foreach ($subclassArr as $subClassCd => $subClassName) {
                        if (in_array($subClassCd, array("TOTAL", "AVG", "CLASS_RANK", "COURSE_RANK", "GRADE_RANK"), true)) {
                            $tmp = $schInfoArr[$subCd999999][$subClassCd];
                            if ($subClassCd == 'AVG' && $tmp != '') {
                                $tmp = sprintf('%.1f', round($tmp , 2));
                            }
                            $contents .= $sep.$tmp;
                        } else {
                            $contents .= $sep.$schInfoArr[$subClassCd]['SCORE'];
                        }
                    }
                    $contents .= $kai;
                }
            }

            $contents .= $kai.$kai;
        }

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($data) {
            $couse = $title;
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>
