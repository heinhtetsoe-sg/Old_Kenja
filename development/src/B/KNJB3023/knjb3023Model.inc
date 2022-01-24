<?php

require_once('for_php7.php');


class knjb3023Model extends Model {
    var $cmd;
    var $year;
    var $semester;
    var $control;
    var $field = array();

    function knjb3023Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->compatible();
        if (VARS::get("PROGRAMID")) {
            unset($this->field);
        }

        $this->cmd = VARS::request("cmd");

        $this->year = CTRL_YEAR;
        $this->semester = CTRL_SEMESTER;
        if (VARS::request("YEAR") != "") {
            $this->year = VARS::request("YEAR");
        }
        $this->staffCd = VARS::post('STAFFCD');
        $this->staffClass = VARS::post('STAFF_CLASS');
        $this->bscSeq = strlen(VARS::post('BSCSEQ')) > 0 ? VARS::post('BSCSEQ') : $this->bscSeq;
        $this->field['PTRN_UPDATE_TITLE'] = VARS::post('ptrnUpdate_TITLE') ? VARS::post('ptrnUpdate_TITLE') : $this->field['PTRN_UPDATE_TITLE'];
        $this->field['PTRN_UPDATE_RADIO'] = VARS::post('PTRN_UPDATE_RADIO') ? VARS::post('PTRN_UPDATE_RADIO') : $this->field['PTRN_UPDATE_RADIO'];

        $this->field['GRADE_SELECTED'] = VARS::request("GRADE_SELECTED");
        $this->field['GRAND_HR_CLASSCD_SELECTED'] = VARS::request("GRAND_HR_CLASSCD_SELECTED");
        $this->field['GRAND_HR_CLASSCD'] = VARS::request("GRAND_HR_CLASSCD");
        $this->field['CLASSCD'] = VARS::post('CLASSCD');
        $this->field['SCHOOL_KIND'] = VARS::post('CLASSCD');
        
        $this->operationRadio = VARS::post("OPERATION_RADIO");

        $this->updateAddData = VARS::request("updateAddData");
        if (isset($this->updateAddData)) {
            $this->updateAddData = json_decode($this->updateAddData, true);
        }
        $this->updateDelData = VARS::request("updateDelData");
        if (isset($this->updateDelData)) {
            $this->updateDelData = json_decode($this->updateDelData, true);
        }

        if($this->cmd == 'update'){
            unset($this->grandHrClassCd);
        }

        $this->ajaxParam = VARS::request("AJAX_PARAM");
        if (isset($this->ajaxParam)) {
            $this->ajaxParam = json_decode($this->ajaxParam, true);
        }

        $this->visibleLine = VARS::post("visibleLine");
        if ($this->cmd == '' || $this->cmd == 'edit') {
            unset($this->visibleLine);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //曜日基本時間割用
        $this->weekPtrn = array();
        $this->weekPtrn[] = array("RENBAN" => "0", "CD" => "2", "DATE_OR_WEEK" => "2", "WEEK_JP" => "月", 'CSS' => 'week_getu');
        $this->weekPtrn[] = array("RENBAN" => "1", "CD" => "3", "DATE_OR_WEEK" => "3", "WEEK_JP" => "火", 'CSS' => 'week_ka');
        $this->weekPtrn[] = array("RENBAN" => "2", "CD" => "4", "DATE_OR_WEEK" => "4", "WEEK_JP" => "水", 'CSS' => 'week_sui');
        $this->weekPtrn[] = array("RENBAN" => "3", "CD" => "5", "DATE_OR_WEEK" => "5", "WEEK_JP" => "木", 'CSS' => 'week_moku');
        $this->weekPtrn[] = array("RENBAN" => "4", "CD" => "6", "DATE_OR_WEEK" => "6", "WEEK_JP" => "金", 'CSS' => 'week_kin');
        $this->weekPtrn[] = array("RENBAN" => "5", "CD" => "7", "DATE_OR_WEEK" => "7", "WEEK_JP" => "土", 'CSS' => 'week_do');
        $this->weekPtrn[] = array("RENBAN" => "6", "CD" => "1", "DATE_OR_WEEK" => "1", "WEEK_JP" => "日", 'CSS' => 'week_niti');

    }

    function compatible() {
        if(!function_exists("json_encode")) {
            function json_encode($param) {
                require_once("JSON.php");
                $json = new Services_JSON();
                return $json->encode($param);
            }
            function json_decode($param, $assoc = false) {
                require_once("JSON.php");
                $json = new Services_JSON($assoc ? SERVICES_JSON_LOOSE_TYPE : 0);
                return $json->decode($param);
            }
        } 
    }

    function fusemozi($staffCd){
        list($simo, $fuseji) = explode(" | ", $this->Properties["showMaskStaffCd"]);
        $ume = "" ;
        for ($umecnt = 1; $umecnt <= strlen($staffCd) - (int)$simo; $umecnt++) {
            $ume .= $fuseji;
        }
        if ($fuseji) {
            return $ume.substr($staffCd, (strlen($staffCd) - (int)$simo), (int)$simo);
        } else {
            return $staffCd;
        }
    }

    //入力項目チェック
    function validateInput() {
        return true;
    }

    //追加および更新
    function &getUpdateModel() {
        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        // ヘッダレコード編集
        $bscSeq = knjb3023Query::updatePtrnTemplate($db, $this);
        $this->bscSeq = $bscSeq;

        if (!empty($this->updateAddData) || !empty($this->updateDelData)) {
            knjb3023Query::update($db, $this);
        }

        $db->commit();
        //DB切断
        Query::dbCheckIn($db);

        $this->setMessage("MSG201");
    }

    //CSV出力
    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        //初期化
        $contents = "";
        $array = array();

        // 校時取得
        $query = knjb3023Query::getPeriod($this);
        $result = $db->query($query);
        $this->periYomikae = array();
        $periodArray = array();
        $periCnt = 1;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $period = array();
            $period["PERI_NAME"] = $row["LABEL"];
            $period["PERI_ID"] = $row["VALUE"];
            $periodArray[] = $period;
            $this->periYomikae[$periCnt] = $row["VALUE"];
            $periCnt++;
        }
        $result->free();


        //ヘッダ出力
        $header1 = array();
        $header1[] = "教科";
        $header1[] = "教員";
        $header2 = array();
        $header2[] = "";
        $header2[] = "";
        for ($i=0; $i < get_count($this->weekPtrn); $i++) { 
            $week = $this->weekPtrn[$i];

            $header1[] = $week["WEEK_JP"];
            for ($j=0; $j < get_count($periodArray); $j++) { 
                $period = $periodArray[$j];
                $header2[] = $period["PERI_NAME"];
                // 校時用のカンマを追加
                if ($j > 0) {
                    $header1[] = "";
                }
            }
        }
        $contents .= implode($header1, ",")."\n";
        $contents .= implode($header2, ",")."\n";

// var_dump($this->year);
// var_dump($this->bscSeq);

        //データ出力
        $data = false;

        // 科目別基本時間割 登録データ取得
        $query = knjb3023Query::getPtrnSubclassDat($this);
        $result = $db->query($query);
        $schSubclassArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schSubclassArray[$row['CLASSCD']][$row['SCHOOL_KIND']][$row['STAFFCD']][$row['WEEK_CD']][$row['PERIODCD']][] = $row;
        }
        $result->free();
        // データ存在判定
        if (get_count($schSubclassArray) > 0) {
            $data = true;
        }

        // 教科－職員(縦ループ用)
        $query = knjb3023Query::getClassStaff($this);
        $result = $db->query($query);
        $classStaffArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $classStaff = array();
            $classStaff["CLASSCD"]     = $row["CLASSCD"];
            $classStaff["SCHOOL_KIND"] = $row["SCHOOL_KIND"];
            $classStaff["CLASSNAME"]   = $row["CLASSNAME"];
            $classStaff["CLASSABBV"]   = $row["CLASSABBV"];
            $classStaff["STAFFCD"]     = $row["STAFFCD"];
            $classStaff["STAFFNAME"]   = $row["STAFFNAME"];

            $classStaffArray[] = $classStaff;
        }
        $result->free();

        // 縦ループ（教科・教員）
        for ($classCnt=0; $classCnt < get_count($classStaffArray); $classCnt++) {
            $classStaff = $classStaffArray[$classCnt];
            $lineData = array();
            // 教科・教員を設定
            $lineData[] = $classStaff["CLASSNAME"];
            $lineData[] = $classStaff["STAFFNAME"];
            // 曜日ループ
            for ($weekCnt=0; $weekCnt < get_count($this->weekPtrn); $weekCnt++) {
                $week = $this->weekPtrn[$weekCnt];
                for ($periodCnt=0; $periodCnt < get_count($periodArray); $periodCnt++) {
                    $period = $periodArray[$periodCnt];
                    // クラスが登録されているか判定
                    if (isset($schSubclassArray[$classStaff['CLASSCD']]
                                                [$classStaff['SCHOOL_KIND']]
                                                [$classStaff['STAFFCD']]
                                                [$week['CD']][$period['PERI_ID']])) {

                        $schValues = $schSubclassArray[$classStaff['CLASSCD']]
                                                    [$classStaff['SCHOOL_KIND']]
                                                    [$classStaff['STAFFCD']]
                                                    [$week['CD']][$period['PERI_ID']];

                        // セルVal初期化
                        $values = array();
                        $valCnt = 1;
                        $hrClassName = "";
                        $subClass = "";
                        foreach ($schValues as $value) {
                            // セルの表示文字成形
                            if ($valCnt > 1) {
                                $hrClassName .= "/";
                            }
                            $hrClassName .= $value['HR_NAME'];
                            $subClass = $value['SUBCLASSNAME'];
                            $valCnt++;
                        }
                        $lineData[] = $hrClassName."　".$subClass;
                    } else {
                        $lineData[] = "";
                    }
                }
            }
            $contents .= implode($lineData, ",")."\n";
        }



        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($data) {
            $title = $this->year."年度 科目基本時間割（職員まとめ）";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
