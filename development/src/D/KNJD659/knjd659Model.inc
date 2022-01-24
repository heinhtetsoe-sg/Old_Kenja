<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd659Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $class_selected; 
    var $schoolName;

    /**
     * メンバ変数の初期化
     */
    function knjd659Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("DATE"             =>  VARS::post("DATE"),             //出欠集計日付
                             "YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期コード
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "FORM_SELECT"      =>  VARS::post("FORM_SELECT"),      //フォーム種別
                             "LATE_EARLY_SEMES" =>  VARS::post("LATE_EARLY_SEMES"), //遅刻・早退は日数を表示
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        //DB接続
        $db = Query::dbCheckOut();

        //学校名取得
        $query = knjd659Query::getSchoolName();
        $this->schoolName = $db->getOne($query);

        //DB切断
        Query::dbCheckIn($db);

        $this->cmd = VARS::request("cmd");
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
    
    //CSV出力処理
    function getDownloadModel() {

        //DB接続
        $db = Query::dbCheckOut();

        //校種の取得
        $query = knjd659Query::getSchregRegdGdat($this,"SCHOOL_KIND");
        $schoolKind = $db->getOne($query);

        //年度
        $nendo = common::DateConv1(str_replace("-","/",CTRL_DATE),2);

        $contents = "";

        //ヘッダ（1行目：タイトル）
        $query = knjd659Query::getSemesterMst($this->field["SEMESTER"],"SEMESTERNAME");
        $semes = $db->getOne($query);
        $title  = $nendo."度 ";
        $title .= $semes."観点別成績一覧表";
        $ary = explode("-",CTRL_DATE);
        $date = $nendo.$ary[1]."月".$ary[2]."日";
        $contents .= $title. str_repeat(",", 5). "作成日：". $date ."\n";

        //ヘッダ（2行目）
        $contents .= "学級,出席番号,氏名";
        $dataTemp = array();
        $query = knjd659Query::getJviewSql($this);
        $result = $db->query($query);
        $sep = ",";
        $hyoutei = "";
        $subClassCd = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればピリオドに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",",".",$row);

            if($subClassCd != "" && $subClassCd != $row["SUBCLASSCD"]){
                //評定列を追加
                $contents .= $sep.$hyoutei;
            }
            $contents .= $sep.$row["VIEWNAME"];

            $subClassCd = $row["SUBCLASSCD"];
            $hyoutei = $row["CLASSNAME"]."評定";
        }
        $result->free();
        $contents .= $sep.$hyoutei."\n";

        //明細
        $this->class_selected = VARS::post("HID_CLASS_SELECTED");
        $seme = $this->field["SEMESTER"] == "9" ? CTRL_SEMESTER : $this->field["SEMESTER"];
        if ($this->class_selected != "") {
            $this->class_selected = explode(",", $this->class_selected);
            for ($i = 0; $i < get_count($this->class_selected); $i++) {
                //SQL文発行
                $dataTemp = array();
                $query = knjd659Query::getStudentInfo($this, $schoolKind, $this->class_selected[$i], $seme);
                $result = $db->query($query);
                $sep = "";
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    //データ内にカンマがあればピリオドに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row = str_replace(",",".",$row);

                    //生徒情報
                    $contents .= $row["HR_NAME"].",".$row["ATTENDNO"].",".$row["NAME"];
                    $sep = ",";

                    //観点(明細)
                    $query = knjd659Query::getJviewStatSql($this, $row["SCHREGNO"]);
                    $result2 = $db->query($query);
                    $subClassCd = "";
                    $electdiv = "";
                    while ($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
                        //評定
                        if($subClassCd != "" && $subClassCd != $row2["SUBCLASSCD"]){
                            $query = knjd659Query::getHyoukaSql($this, $row["SCHREGNO"], $subClassCd);
                            $status = $db->getOne($query);
                            if ($this->Properties["useRecordDat"] != 'KIN_RECORD_DAT') {
                                if($electdiv == "1"){
                                    $status = "";
                                    if($status == "11") $status = "A";
                                    if($status == "22") $status = "B";
                                    if($status == "33") $status = "C";
                                }
                            }
                            $contents .= $sep.$status;
                        }

                        //観点
                        $contents .= $sep.$row2["STATUS"];
                        $subClassCd = $row2["SUBCLASSCD"];
                        $electdiv = $row2["ELECTDIV"];
                    }
                    $result2->free();

                    //評定(最終科目)
                    $query = knjd659Query::getHyoukaSql($this, $row["SCHREGNO"], $subClassCd);
                    $status = $db->getOne($query);
                    if ($this->Properties["useRecordDat"] != 'KIN_RECORD_DAT') {
                        if($electdiv == "1"){
                            $status = "";
                            if($status == "11") $status = "A";
                            if($status == "22") $status = "B";
                            if($status == "33") $status = "C";
                        }
                    }
                    $contents .= $sep.$status;
                    $contents .= "\n";
                    $data = true;
                }
                $result->free();
            }
        }

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($data) {
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
