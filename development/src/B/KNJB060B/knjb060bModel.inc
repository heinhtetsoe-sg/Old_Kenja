<?php

require_once('for_php7.php');

class knjb060bModel extends Model
{
    public $field = array();
    public $cmd;
    public $control;//コントロールマスタ
    public $staffcd;//職員コード
    public $sDate;  //開始日
    public $eDate;  //終了日

    /**
     * メンバ変数の初期化
     */
    public function knjb060bModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;//職員コード
    }

    public function init()
    {
        $this->field = array("SCHOOL_KIND"   => VARS::post("SCHOOL_KIND"),
                             "START_DATE"    => VARS::post("START_DATE"),
                             "END_DATE"      => VARS::post("END_DATE"),
                             "OUTPUT_SELECT" => VARS::post("OUTPUT_SELECT"),
                             "DBNAME"        => VARS::post("DBNAME")
        );
        $this->sDate = str_replace("/", "-", $this->field["START_DATE"]);
        $this->eDate = str_replace("/", "-", $this->field["END_DATE"]);

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    /**
     * 1日分の時間割のCSV文字列を取得
     */
    private function getCsvContent($printData, $classList)
    {
        $lectires = array();

        //学年、クラス別のデータを1つにまとめる
        for ($grd = 0; $grd < get_count($printData->grade); $grd++) {
            foreach ($printData->grade[$grd]->lecturesByGrade as $hrClass) {
                $lectires[] = $hrClass;
            }
        }

        $DUMMY = "\b";//通常使用しない文字ならなんでも良い
        $INIT_ROW = 20;
        $INIT_COL = get_count($lectires) + 1;  //クラス数 + 時限列
        $CSV_EL = "\r\n";//CSVとしての改行コード
        $MAX_PERIOD = 9;

        //データ配列の枠だけ作成する。ブランクと区別するためダミーの文字列で埋める
        $contentData = array();
        for ($i = 0; $i < $INIT_ROW; $i++) {
            for ($j = 0; $j < $INIT_COL; $j++) {
                $contentData[$i][$j] = $DUMMY;
            }
        }

        //CSVデータにセット
        //日付
        $wday = array("日","月","火","水","木","金","土");
        $date = explode("-", $printData->printDate);
        $w = date("w", strtotime($printData->printDate));
        $contentData[0][0] = (int)$date[1]."月".(int)$date[2]."日（".$wday[$w]."）変更時間割";
        $contentData[0][1] = "最終更新：".str_replace("-", "/", $printData->updateDate);
        //時限
        for ($period = 1; $period <= $MAX_PERIOD; $period++) {
            $contentData[$period * 2][0] = $period;
        }
        //クラス
        //科目がないクラスも出力
        for ($class = 0; $class <= get_count($classList); $class++) {
            //クラス名
            $contentData[1][$class + 1] = $classList[$class];
            for ($lectire = 0; $lectire <= get_count($lectires); $lectire++) {
                $lbc = $lectires[$lectire];
                if ($classList[$class] == $lbc->hrClassName) {
                    //時限
                    for ($period = 1; $period <= $MAX_PERIOD; $period++) {
                        //変更前科目
                        $contentData[$period * 2][$class + 1] = $lbc->lectures[$period]->beforeSubclassName;
                        //変更後科目
                        if ($lbc->lectures[$period]->beforeSubclassName != $lbc->lectures[$period]->afterSubclassName) {
                            $contentData[$period * 2 + 1][$class + 1] = $lbc->lectures[$period]->afterSubclassName;
                        }
                    }
                }
            }
        }
        $contents = "";
        //カンマ区切りの文字列に変換する
        foreach ($contentData as $rowKey => $rowVal) {
            foreach ($rowVal as $colKey => $colVal) {
                //ダミー文字は削除
                if ($colVal == $DUMMY) {
                    $contentData[$rowKey][$colKey] = "";
                }

                //文字列中の改行コードはLFに統一する
                $contentData[$rowKey][$colKey] = str_replace("\r\n", "\n", $contentData[$rowKey][$colKey]);//CR+LF→LFに統一
                $contentData[$rowKey][$colKey] = str_replace("\r", "\n", $contentData[$rowKey][$colKey]);//CR   →LFに統一

                //文字列中に改行コードを含む場合は、文字列自体をダブルクォーテーションで囲む
                if (substr_count($contentData[$rowKey][$colKey], "\n") > 0) {
                    $contentData[$rowKey][$colKey] = "\"" . $contentData[$rowKey][$colKey] . "\"";
                }
            }

            $contents .= (implode(',', $contentData[$rowKey]) . $CSV_EL);
        }

        return $contents;
    }

    /**
     * CSVダウンロード
     */
    public function getDownloadModel()
    {
        //時間割取得
        $timeTableList = $this->setTimeTableList();

        //変更前情報取得
        $beforeList = $this->setBeforeList();

        //変更後情報取得
        $afterList = $this->setAfterList();

        if (($timeTableList == false) && ($beforeList == false) && ($afterList == false)) {
            //取得データがない時
            $this->setWarning("MSG303");
            return false;
        }

        //CSV用にデータを整形
        $classList = array();
        $datePrintData = $this->setPrintDataList($timeTableList, $beforeList, $afterList, $classList);
        sort($classList);

        //日付毎のCSV情報を取得
        foreach ($datePrintData as $printData) {
            $contentsByDate[] = $this->getCsvContent($printData, $classList);
        }
        $glue = $CSV_EL;//各日付の間に改行を挟む

        $filename = sprintf("%s.csv", str_replace("-", "", CTRL_DATE).'変更時間割表');
        $contents = implode($glue, $contentsByDate);
        common::downloadContents($contents, $filename, 'SJIS-win');

        return true;
    }

    //時間割取得
    public function setTimeTableList()
    {
        $timeTableList = array();

        //DB接続
        $db = Query::dbCheckOut();

        //DBからデータ取得
        $timeTable = $this->getDataWithSql($db, knjb060bQuery::sqlTimetable($this));

        foreach ($timeTable as $val) {
            $dupCheck = false;
            for ($i = 0; $i < get_count($timeTableList); $i++) {
                $temp = $timeTableList[$i];
                if ($temp->executeDate == $val['EXECUTEDATE']
                    && $temp->grade    == $val['GRADE']
                    && $temp->hrClass  == $val['HR_CLASS']
                    && $temp->periodCd == $val['PERIODCD']) {
                    if ((is_null($val['SUBCLASSABBV']) == false)) {
                        $temp->subclassAbbv = $temp->subclassAbbv."、".$val['SUBCLASSABBV'];
                    }
                    $timeTableList[$i] = $temp;
                    $dupCheck = true;
                    break;
                }
            }
            if ($dupCheck == false) {
                $timeTableList[] = new TimeTable(
                    $val['EXECUTEDATE'],
                    $val['UPDATED'],
                    $val['PERIODCD'],
                    $val['CHAIRCD'],
                    $val['GRADE'],
                    $val['HR_CLASS'],
                    $val['HR_NAMEABBV'],
                    $val['GROUPCD'],
                    $val['SUBCLASSABBV']
                );
            }
        }
        Query::dbCheckIn($db);
        return $timeTableList;
    }

    //変更前情報取得
    public function setBeforeList()
    {
        $beforeList = array();

        //DB接続
        $db = Query::dbCheckOut();

        //DBからデータ取得
        $beforeTimeTable = $this->getDataWithSql($db, knjb060bQuery::sqlBeforeTimetable($this));

        foreach ($beforeTimeTable as $val) {
            $dupCheck = false;
            for ($i = 0; $i < get_count($beforeList); $i++) {
                $temp = $beforeList[$i];
                if ($temp->fromDate        == $val['FROM_DATE']
                    && $temp->grade        == $val['GRADE']
                    && $temp->hrClass      == $val['HR_CLASS']
                    && $temp->fromPeriodCd == $val['FROM_PERIODCD']) {
                    if ((is_null($val['SUBCLASSABBV']) == false)) {
                        $temp->subclassAbbv = $temp->subclassAbbv."、".$val['SUBCLASSABBV'];
                    }
                    $beforeList[$i] = $temp;
                    $dupCheck = true;
                    break;
                }
            }
            if ($dupCheck == false) {
                $beforeList[] = new BeforeTimeTable(
                    $val['GRADE'],
                    $val['HR_CLASS'],
                    $val['HR_NAMEABBV'],
                    $val['GROUPCD'],
                    $val['FROM_DATE'],
                    $val['FROM_PERIODCD'],
                    $val['FROM_CHAIRCD'],
                    $val['SUBCLASSABBV']
                );
            }
        }
        Query::dbCheckIn($db);
        return $beforeList;
    }

    //変更後情報取得
    public function setAfterList()
    {
        $afterList = array();

        //DB接続
        $db = Query::dbCheckOut();

        //DBからデータ取得
        $afterTimeTable = $this->getDataWithSql($db, knjb060bQuery::sqlAfterTimetable($this));

        foreach ($afterTimeTable as $val) {
            $dupCheck = false;
            for ($i = 0; $i < get_count($afterList); $i++) {
                $temp = $afterList[$i];
                if ($temp->toDate        == $val['TO_DATE']
                    && $temp->grade      == $val['GRADE']
                    && $temp->hrClass    == $val['HR_CLASS']
                    && $temp->toPeriodCd == $val['TO_PERIODCD']) {
                    if ((is_null($val['SUBCLASSABBV']) == false)) {
                        $temp->subclassAbbv = $temp->subclassAbbv."、".$val['SUBCLASSABBV'];
                    }
                    $afterList[$i] = $temp;
                    $dupCheck = true;
                    break;
                }
            }
            if ($dupCheck == false) {
                $afterList[] = new AfterTimeTable(
                    $val['GRADE'],
                    $val['HR_CLASS'],
                    $val['HR_NAMEABBV'],
                    $val['GROUPCD'],
                    $val['GRADE'],
                    $val['FROM_DATE'],
                    $val['TO_DATE'],
                    $val['TO_PERIODCD'],
                    $val['TO_CHAIRCD'],
                    $val['TOSUBCLASSNAME']
                );
            }
        }
        Query::dbCheckIn($db);
        return $afterList;
    }

    //CSV用にデータを整形
    public function setPrintDataList($basicList, $beforeList, $afterList, &$classList)
    {
        $className = "";
        $date = "";
        $upDate = "";
        $pd = new PrintData();
        $datePrintData = array();

        foreach ($basicList as $basic) {
            $lbc = new LecturesByClass();
            $lbc->hrClass = $basic->hrNameAbbv;
            $lbc->hrClassName = str_replace("-", "年", $basic->hrNameAbbv)."組";
            if (in_array($lbc->hrClassName, $classList) == false) {
                $classList[] = $lbc->hrClassName;
            }
            $pdGrade = $basic->grade - 1;

            //一番初めの処理
            if ("" == $className) {
                if (is_null($basic->subclassAbbv) == false) {
                    $lbc->lectures[$basic->periodCd - 1]->beforeSubclassName = $basic->subclassAbbv;
                    if (array_key_exists($lbc->hrClassName, $pd->grade[$pdGrade]->lecturesByGrade) == false) {
                        $pd->grade[$pdGrade]->lecturesByGrade[$lbc->hrClassName] = $lbc;
                    } else {
                        $pd->grade[$pdGrade]->lecturesByGrade[$lbc->hrClassName]->lectures[$basic->periodCd - 1]->beforeSubclassName = $basic->subclassAbbv;
                    }
                }
                $className = $basic->hrClass;
                $lbc = $pd->grade[$pdGrade]->lecturesByGrade[$lbc->hrClassName];
                if (is_null($lbc) == false) {
                    //変更前科目を保持
                    $this->printBefore($basic, $beforeList, $lbc);
                    //変更後科目を保持
                    $this->printAfter($basic, $afterList, $lbc);
                }
            } else {
                //日付が同じとき
                if ($date == $basic->executeDate) {
                    //クラスが同じとき
                    if ($className == $basic->hrClass && (is_null($basic->subclassAbbv) == false)) {
                        $lbc->lectures[$basic->periodCd - 1]->beforeSubclassName = $basic->subclassAbbv;
                        if (array_key_exists($lbc->hrClassName, $pd->grade[$pdGrade]->lecturesByGrade) == false) {
                            $pd->grade[$pdGrade]->lecturesByGrade[$lbc->hrClassName] = $lbc;
                        } else {
                            $pd->grade[$pdGrade]->lecturesByGrade[$lbc->hrClassName]->lectures[$basic->periodCd - 1]->beforeSubclassName = $basic->subclassAbbv;
                        }
                    } elseif (is_null($basic->subclassAbbv) == false) {
                        $lbc->lectures[$basic->periodCd - 1]->beforeSubclassName = $basic->subclassAbbv;
                        if (array_key_exists($lbc->hrClassName, $pd->grade[$pdGrade]->lecturesByGrade) == false) {
                            $pd->grade[$pdGrade]->lecturesByGrade[$lbc->hrClassName] = $lbc;
                        } elseif (is_null($basic->subclassAbbv) == false) {
                            $pd->grade[$pdGrade]->lecturesByGrade[$lbc->hrClassName]->lectures[$basic->periodCd - 1]->beforeSubclassName = $basic->subclassAbbv;
                        }
                    }
                }
                $lbc = $pd->grade[$pdGrade]->lecturesByGrade[$lbc->hrClassName];
                if (is_null($lbc) == false) {
                    //変更前科目を保持
                    $this->printBefore($basic, $beforeList, $lbc);
                    //変更後科目を保持
                    $this->printAfter($basic, $afterList, $lbc);
                }
            }
            $className = $basic->hrClass;

            //日付が変わったとき
            if ($date != $basic->executeDate && "" != $date) {
                if ((is_null($upDate) == false) && strlen($upDate) == 26) {
                    $upDate = substr($upDate, 5);
                    $upDate = substr($upDate, 0, 11);
                } else {
                    $upDate = "";
                }
                $pd->printDate  = $date;
                $pd->updateDate = $upDate;
                if (array_key_exists($date, $datePrintData) == false) {
                    $datePrintData[$date] = $pd;
                }
                $pd = new PrintData();
            }
            $date   = $basic->executeDate;
            $upDate = $basic->updated;
        }
        if ((is_null($upDate) == false) && strlen($upDate) == 26) {
            $upDate = substr($upDate, 5);
            $upDate = substr($upDate, 0, 11);
        } else {
            $upDate = "";
        }
        $pd->printDate  = $date;
        $pd->updateDate = $upDate;
        if (array_key_exists($date, $datePrintData) == false) {
            $datePrintData[$date] = $pd;
        }
        return $datePrintData;
    }

    //変更前科目
    private function printBefore($basic, $beforeList, $lbc)
    {
        foreach ($beforeList as $before) {
            if ((is_null($before->fromDate) == false)
                && $basic->executeDate      == $before->fromDate
                && $basic->grade            == $before->grade
                && $basic->hrClass          == $before->hrClass) {
                //変更前のデータを保持
                $lbc->lectures[$before->fromPeriodCd - 1]->beforeSubclassName = $before->subclassAbbv;
            }
        }
    }

    //変更後科目
    private function printAfter($basic, $afterList, $lbc)
    {
        foreach ($afterList as $after) {
            if ($basic->executeDate == $after->toDate
                && $basic->grade    == $after->grade
                && $basic->hrClass  == $after->hrClass
                && $basic->chairCd  != $after->toChairCd) {
                //変更後のデータを保持
                $lbc->lectures[$after->toPeriodCd - 1]->afterSubclassName = $after->toSubclassName;
            }
        }
    }

    /**
     * 引数のSQLでDBからデータ取得
     */
    private function getDataWithSql($db, $sql)
    {
        $ret = array();

        $result = $db->query($sql);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $ret[]= $row;
        }
        $result->free();

        return $ret;
    }

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}

/** 時間割 */
class TimeTable
{
    public $executeDate;
    public $updated;
    public $periodCd;
    public $chairCd;
    public $grade;
    public $hrClass;
    public $hrNameAbbv;
    public $groupCd;
    public $subclassAbbv;

    public function __construct(
        $executeDate,
        $updated,
        $periodCd,
        $chairCd,
        $grade,
        $hrClass,
        $hrNameAbbv,
        $groupCd,
        $subclassAbbv
    ) {
        $this->executeDate  = $executeDate;
        $this->updated      = $updated;
        $this->periodCd     = $periodCd;
        $this->chairCd      = $chairCd;
        $this->grade        = $grade;
        $this->hrClass      = $hrClass;
        $this->hrNameAbbv   = $hrNameAbbv;
        $this->groupCd      = $groupCd;
        $this->subclassAbbv = $subclassAbbv;
    }
}

/** 変更前情報 */
class BeforeTimeTable
{
    public $grade;
    public $hrClass;
    public $hrNameAbbv;
    public $groupCd;
    public $fromDate;
    public $fromPeriodCd;
    public $fromChairCd;
    public $subclassAbbv;

    public function __construct(
        $grade,
        $hrClass,
        $hrNameAbbv,
        $groupCd,
        $fromDate,
        $fromPeriodCd,
        $fromChairCd,
        $subclassAbbv
    ) {
        $this->grade        = $grade;
        $this->hrClass      = $hrClass;
        $this->hrNameAbbv   = $hrNameAbbv;
        $this->groupCd      = $groupCd;
        $this->fromDate     = $fromDate;
        $this->fromPeriodCd = $fromPeriodCd;
        $this->fromChairCd  = $fromChairCd;
        $this->subclassAbbv = $subclassAbbv;
    }
}

/** 変更後情報 */
class AfterTimeTable
{
    public $grade;
    public $hrClass;
    public $hrNameAbbv;
    public $groupCd;
    public $grade2;
    public $fromDate;
    public $toDate;
    public $toPeriodCd;
    public $toChairCd;
    public $toSubclassName;

    public function __construct(
        $grade,
        $hrClass,
        $hrNameAbbv,
        $groupCd,
        $grade2,
        $fromDate,
        $toDate,
        $toPeriodCd,
        $toChairCd,
        $toSubclassName
    ) {
        $this->grade          = $grade;
        $this->hrClass        = $hrClass;
        $this->hrNameAbbv     = $hrNameAbbv;
        $this->groupCd        = $groupCd;
        $this->grade2         = $grade2;
        $this->fromDate       = $fromDate;
        $this->toDate         = $toDate;
        $this->toPeriodCd     = $toPeriodCd;
        $this->toChairCd      = $toChairCd;
        $this->toSubclassName = $toSubclassName;
    }
}

//特定クラス・特定時限の授業情報
class Lecture
{
    public $beforeSubclassName;//変更前：科目名
    public $afterSubclassName; //変更後：科目名
}

//特定クラスの1日の授業
class LecturesByClass
{
    public $hrClass;      //学年クラス
    public $hrClassName;  //クラス名
    public $lectures = array();

    public function __construct()
    {
        $MAX_PERIOD = 9;
        for ($i = 0; $i < $MAX_PERIOD; $i++) {
            $this->lectures[$i] = new Lecture();
        }
    }
}

//学年ごとの授業
class LecturesByGrade
{
    public $lecturesByGrade = array();
}

//CSV出力データ用クラス
class PrintData
{
    public $printDate;        //該当日付
    public $updateDate;       //最終更新日時
    public $grade = array();  //3学年分を保持

    public function __construct()
    {
        $MAX_GRADE = 3;
        for ($i = 0; $i < $MAX_GRADE; $i++) {
            $this->grade[$i] = new LecturesByGrade();
        }
    }
}
