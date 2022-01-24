<?php

require_once('for_php7.php');

require_once("AttendAccumulate.php");

class knjd290Model extends Model {

    var $cmd;
    var $subclasscd;
    var $belonging_div;
    var $restrict;
    var $selectdata;
    var $control_data = array();    //コントロールマスタデータを保持
    var $isSougou;
    var $sougouMajor;

    function knjd290Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->restrict = (AUTHORITY == DEF_REFER_RESTRICT) || (AUTHORITY == DEF_UPDATE_RESTRICT) ? true : false;
        $this->selectdata   = VARS::post("selectdata");
        $this->date = VARS::post("DATE");

        if ($this->cmd == "belong") {
            unset($this->subclasscd);
        }

        if ($this->cmd == "") {
            unset($this->belonging_div);
            unset($this->subclasscd);
        }

        //プロパティーファイル読込
        $this->getPropertiesAll();
    }

    //入力項目チェック
    function validateInput()
    {
        return true;
    }

    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $definecode["schoolmark"]        = $this->Properties["schoolmark"];
        $definecode["useschchrcountflg"] = $this->Properties["useschchrcountflg"];
        $definecode["usefromtoperiod"]   = $this->Properties["usefromtoperiod"];
        $definecode["useabsencehigh"]    = $this->Properties["useabsencehigh"];

        //固定
        $SSEMESTER = "1";
        $year = CTRL_YEAR;
        $semester = CTRL_SEMESTER;
        //近大判定用
        $query = knjd290Query::getNameMst($year, "Z010", "00");
        $z010 = $db->getOne($query);
        //学校マスタ
        $query = knjd290Query::getSchoolMst();
        $defineSchoolCode = $db->getRow($query, DB_FETCHMODE_ASSOC);
        //校種、学校コード
        $schoolcd = $school_kind = "";
        if ($db->getOne(knjd290Query::checkSchoolMst()) > 0) {
            if ($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $schoolcd    = sprintf("%012d", SCHOOLCD);
                $school_kind = SCHOOLKIND;
            }
        }
        //出欠関連共通
        $attendSemAllMap = AttendAccumulate::getAttendSemesMap($db, $z010, $year);
        $periodInState = AttendAccumulate::getPeiodValue($db, $definecode, $year, $SSEMESTER, $semester);
        $hasuuMap = AttendAccumulate::getHasuuMap($attendSemAllMap, $year."-04-01", str_replace("/", "-", $this->date));
        $knjSchoolMst = AttendAccumulate::getSchoolMstMap($db, $year, $schoolcd, $school_kind);
        //出欠取得
        $attendSql = $this->attendSubclass($db,
                                           $definecode,
                                           $SSEMESTER,
                                           $year,
                                           $semester,
                                           $z010,
                                           $defineSchoolCode,
                                           $knjSchoolMst,
                                           $attendSemAllMap,
                                           $periodInState,
                                           $hasuuMap);
        //必履修科目
        $hituRisyu = array();
        $query = knjd290Query::getHituRisyu($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $seq            = $row["SEQ"];
            $classcd        = $row["CLASSCD"];
            $subclasscd     = $row["SUBCLASSCD"];
            $coursecd       = $row["COURSECD"];
            $majorcd        = $row["MAJORCD"];

            $hituRisyu[2][$coursecd.$majorcd][$classcd][$seq][] = $classcd . $subclasscd;
        }
        $result->free();

        //単位マスタ
        $creditMst = array();
        $query = knjd290Query::getCreditMst();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $key  = $row["COURSECD"];
            $key .= $row["MAJORCD"];
            $key .= $row["GRADE"];
            $key .= $row["COURSECODE"];
            $key .= $row["CLASSCD"];
            $key .= $row["SUBCLASSCD"];

            $creditMst[$key] = $row;
        }
        $result->free();

        //標準単位マスタ
        $stanCreditMst = array();
        $query = knjd290Query::getStandardCredit($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $key  = $row["CLASSCD"];
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $key .= $row["SCHOOL_KIND"];
            }
            $key .= $row["SUBCLASSCD"];

            $stanCreditMst[$key] = $row["CREDITS"];
        }
        $result->free();

        $gradeArray = explode(",", $this->selectdata);
        foreach ($gradeArray as $val) {
            $this->updateExe($db, $hituRisyu, $creditMst, $stanCreditMst, $val, $attendSql);
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        $this->setMessage("MSG201");
    }

    function attendSubclass($db,
                            $definecode,
                            $SSEMESTER,
                            $year,
                            $semester,
                            $z010,
                            $defineSchoolCode,
                            $knjSchoolMst,
                            $attendSemAllMap,
                            $periodInState,
                            $hasuuMap
    ) {
        //生徒単位で学籍番号を変える為、固定文言にしておく。
        $schregno = "setSchregNo";
        $attendSql = AttendAccumulate::getAttendSubclassSql(
                                    $hasuuMap["semesFlg"],
                                    $definecode,
                                    $defineSchoolCode,
                                    $knjSchoolMst,
                                    $year,
                                    $SSEMESTER,
                                    $semester,
                                    $hasuuMap["attendSemesInState"],
                                    $periodInState,
                                    $hasuuMap["befDayFrom"],
                                    $hasuuMap["befDayTo"],
                                    $hasuuMap["aftDayFrom"],
                                    $hasuuMap["aftDayTo"],
                                    $grade,
                                    $hrClass,
                                    $schregno);

        return $attendSql;
    }

    function updateExe($db, $hituRisyu, $creditMst, $stanCreditMst, $val, $attendSql){
        $query = knjd290Query::getStudent($this, $val);
        $result = $db->query($query);

        while ($schregInfo = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $subClassArray = array();
            $classArray = array();
            $creditTotal = array();
            //今年度出欠OKの科目
            $attendQuery = str_replace("setSchregNo", $schregInfo["SCHREGNO"], $attendSql);
            $result2 = $db->query($attendQuery);
            while ($atRow = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
                $subClassCd = $atRow["SUBCLASSCD"];
                $classCd = substr($atRow["SUBCLASSCD"], 0, 2);
                $key = $schregInfo["KEY"].$subClassCd.$classCd;
                if ($atRow["SEMESTER"] != 9) {
                    continue;
                }
                if ($atRow["ABSENT_SEM"] <= $creditMst[$key]["ABSENCE_HIGH"]) {
                    //教育課程対応 固定で学校校種'H'を設定
                    if ($this->Properties["useCurriculumcd"] == '1') {
                        $subClassArray[] = $classCd . 'H' . $subClassCd;
                    } else {
                        $subClassArray[] = $classCd . $subClassCd;
                    }
                    $classArray[] = $classCd;
                    //教育課程対応 固定で学校校種'H'を設定
                    if ($this->Properties["useCurriculumcd"] == '1') {
                        $creditTotal[$classCd . 'H' . $subClassCd] += $creditMst[$key]["CREDITS"];
                    } else {
                        $creditTotal[$classCd . $subClassCd] += $creditMst[$key]["CREDITS"];
                    }
                }
            }
            $result2->free();

            //過年度修得科目
            $query = knjd290Query::getStudyRec($schregInfo["SCHREGNO"]);
            $result3 = $db->query($query);
            while ($stRow = $result3->fetchRow(DB_FETCHMODE_ASSOC)) {
                //教育課程対応 固定で学校校種'H'を設定
                if ($this->Properties["useCurriculumcd"] == '1') {
                    $subClassArray[] = $stRow["CLASSCD"] . 'H' . $stRow["SUBCLASSCD"];
                } else {
                    $subClassArray[] = $stRow["CLASSCD"] . $stRow["SUBCLASSCD"];
                }
                $classArray[] = $stRow["CLASSCD"];
                $getCre = $stRow["GET_CREDIT"];
                $addCre = $stRow["ADD_CREDIT"];
                //教育課程対応 固定で学校校種'H'を設定
                if ($this->Properties["useCurriculumcd"] == '1') {
                    $creditTotal[$stRow["CLASSCD"] . 'H' . $stRow["SUBCLASSCD"]] += ($getCre + $addCre);
                } else {
                    $creditTotal[$stRow["CLASSCD"] . $stRow["SUBCLASSCD"]] += ($getCre + $addCre);
                }
            }
            $result3->free();
            //重複をカット
            $subArray = array_unique($subClassArray);
            $claArray = array_unique($classArray);
            //教育課程は、とりあえず２固定
            if ($hituRisyu[2][$schregInfo["COURSECD"].$schregInfo["MAJORCD"]] != null) {
                list($errClass, $okClass) = checkThisStudent($schregInfo, $hituRisyu[2], $subArray, $claArray, $creditTotal, $stanCreditMst, $set_useCurriculum, $this);

                updateDB($db, $schregInfo["SCHREGNO"], $errClass, $okClass, $this);
                updateHituRisyuMiTouroku($db, $schregInfo["SCHREGNO"], $this);
            }
        }
        $result->free();
    }
}

function checkThisStudent($schregInfo, $hituRisyu, $subClassArray, $classArray, $creditTotal, $stanCreditMst, &$set_useCurriculum, $model) {
    $errClass = array();
    $okClass = array();
    foreach ($hituRisyu[$schregInfo["COURSECD"].$schregInfo["MAJORCD"]] as $classcd => $seqArray) {
        //教科があるか
        if (!in_array($classcd, $classArray)) {
            //教育課程対応 固定で学校校種'H'を設定
            if ($model->Properties["useCurriculumcd"] == '1') {
                $errClass[] = $classcd.":1:H:".$schregInfo["COURSECD"].":".$schregInfo["MAJORCD"].":";
            } else {
                $errClass[] = $classcd.":1:".$schregInfo["COURSECD"].":".$schregInfo["MAJORCD"].":";
            }
            continue;
        }
        $kyoukaFlag = false;
        $existCodeFlag = false;
        //$codeArray には 教科コード＋課程コード＋科目コードの配列が入っている
        foreach ($seqArray as $seqData => $codeArray) {
            $setSeq = $seqData;
            $getHituRisyuFlag = false;
            //ある履修パターンの科目をそれぞれループ
            foreach ($codeArray as $codeVal) {
                //生徒の取得した科目をループ
                foreach ($subClassArray as $subClassVal) {
                    //対象科目の取得単位と標準単位マスタの単位数を比べる
                    if ($codeVal == $subClassVal && $stanCreditMst[$codeVal] <= $creditTotal[$codeVal]) {
                        $existCodeFlag = true;
                        break;
                    }
                }
                if ($existCodeFlag) { //ある履修パターンのある科目を生徒が履修していたなら
                    $getHituRisyuFlag = true;
                    break;
                }
            }
            if ($getHituRisyuFlag) { //ある履修パターンを満たせれば、次の履修パターンへ（処理を軽くするため）
                $kyoukaFlag = true;
                break;
            }
        }
        //対象教科パターンマッチせず
        //教育課程対応 固定で学校校種'H'を設定
        if ($model->Properties["useCurriculumcd"] == '1') {
            if ($kyoukaFlag) {
                $errClass[] = $classcd.":1:H:".$schregInfo["COURSECD"].":".$schregInfo["MAJORCD"].":".$setSeq;
            } else {
                $okClass[] = $classcd.":0:H:".$schregInfo["COURSECD"].":".$schregInfo["MAJORCD"].":".$setSeq;
            }
        } else {
            if ($kyoukaFlag) {
                $errClass[] = $classcd.":1:".$schregInfo["COURSECD"].":".$schregInfo["MAJORCD"].":".$setSeq;
            } else {
                $okClass[] = $classcd.":0:".$schregInfo["COURSECD"].":".$schregInfo["MAJORCD"].":".$setSeq;
            }
        }
    }
    return array($errClass, $okClass);
}

function updateDB($db, $schregno, $errClass, $okClass, $model) {

    //データ削除
    $query = knjd290Query::getDeleteQuery($schregno);
    $db->query($query);

    foreach ($errClass as $setData) {
        $query = knjd290Query::getInsertQuery($schregno, $setData, $model);
        $db->query($query);
    }

    foreach ($okClass as $setData) {
        $query = knjd290Query::getInsertQuery($schregno, $setData, $model);
        $db->query($query);
    }
}

function updateHituRisyuMiTouroku($db, $schregno, $model) {
    $query = knjd290Query::getInsertHituRisyuMiTouroku($schregno, $model);
    $db->query($query);
}
?>
