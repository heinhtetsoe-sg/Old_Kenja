<?php

require_once('for_php7.php');
class knji100c_03Model extends Model {

    var $cmd;
    var $schregno;                  //学籍番号
    var $control_data = array();    //コントロールマスタデータを保持
    var $item;
    var $selectdata_l;              //対象生徒
    var $selectdata_r;              //書出し項目一覧
    var $koteidata;                 //CSV項目(固定)
    var $opt_subclass  = array();

    function knji100c_03Model() {
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        //CSV固定表示
        $this->kotei = array();
        $this->kotei[]["SCHREGNO"]      = "学籍番号";
        $this->kotei[]["NAME"]          = "生徒氏名";
        $this->kotei[]["GRADE"]         = "学年";
        $this->kotei[]["HR_CLASS"]      = "組";
        $this->kotei[]["SEX"]           = "性別区分(*)";

        //画面表示項目一覧
        $this->item = array();
        $this->item[]["ATTENDNO"]       = "出席番号";
        $this->item[]["MONTH"]          = "月（累積）";
        $this->item[]["KEKKA_INFO"]     = "欠課情報";

        //ITEM
        $this->selectdata_r     = VARS::post("selectdata_r");
        //対象生徒
        $this->selectdata_l     = VARS::post("selectdata_l");
        //課程学科
        $this->coursemajor      = VARS::post("COURSEMAJOR");
        //学年
        $this->grade            = VARS::post("GRADE");
        //性別
        $this->sex              = VARS::post("SEX");
        //年組
        $this->grade_hr_class   = VARS::post("GRADE_HR_CLASS");
        //出力設定
        $this->output           = VARS::post("OUTPUT");
        //対象月
        $this->month            = VARS::post("MONTH");
        //対象月名
        $this->month_name       = VARS::post("MONTH_NAME");

        //サブシステム
        $this->subsystem = (VARS::get("SUBSYSTEM")) ? VARS::get("SUBSYSTEM") : '03';
        //対象生徒
        if(VARS::get("selectdata_l"))
            $this->selectdata_l     = VARS::get("selectdata_l");
        //学年
        if(VARS::get("GRADE"))
            $this->grade            = VARS::get("GRADE");
        //年組
        if(VARS::get("GRADE_HR_CLASS"))
            $this->grade_hr_class   = VARS::get("GRADE_HR_CLASS");
        //出力設定
        if(VARS::get("OUTPUT"))
            $this->output           = VARS::get("OUTPUT");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if (VARS::get("SEND_selectSchoolKind")) {
            $this->selectSchoolKind = VARS::get("SEND_selectSchoolKind");
        }
    }

    //CSV書出し
    function &getCsvModel() {
        $db = Query::dbCheckOut();
        $this->koteidata = "";
        foreach($this->kotei as $key => $val) {
            foreach ($val as $field => $label) {
                $this->koteidata = $this->koteidata.$field.",";
            }
        }
        $item = explode(",", $this->selectdata_r);

        $query = knji100c_03Query::getDelFieldSql($this);
        $db->query($query);
        $setSeq = 1;
        foreach ($item as $key => $val) {
            $query = knji100c_03Query::getFieldInsSql($this, $setSeq, $val);
            $db->query($query);
            $setSeq++;
        }
        $this->selectdata_r = $this->koteidata.$this->selectdata_r;
        $item = explode(",", $this->selectdata_r);

        //項目名一覧
        $item_array = array();
        foreach($this->kotei as $key => $val) {
            foreach ($val as $field => $label) {
                $item_array[$field] = $label; //固定項目
            }
        }
        foreach($this->item as $key => $val) {
            foreach ($val as $field => $label) {
                $item_array[$field] = $label; //書出し項目
            }
        }

        $contents = $sep = "";

        //対象月取得
        list($attend_month, $attend_seme) = preg_split("/-/",$this->month);
        $attend_month = sprintf("%d", $attend_month);
        $month = array();
        for($m = 4; $m <= 15; $m++){
            if ($attend_month < 4) {
                if($m <= ($attend_month + 12)) {
                    $month[] = ($m > 12) ? sprintf("%02d", ($m-12)) : sprintf("%02d", $m); 
                }
            } else {
                if($m <= $attend_month) {
                    $month[] = sprintf("%02d", $m); 
                }
            }
        }

        //ヘッダ作成
        //固定項目+画面からの選択項目
        for($i = 0; $i < get_count($item); $i++){

            if ($this->output == OUT_CODE_NAME){    //コード＋名称
                $codePrefix = str_replace("(*)", "", $item_array[$item[$i]]);
                switch($item[$i]){
                    case "SEX":
                        $contents .= $sep .$codePrefix."コード,".$item_array[$item[$i]];
                        break;
                    case "KEKKA_INFO":
                        $results = $db->query(knji100c_03Query::getSubclassList($this, $attend_seme, $month));
                        $this->opt_subclass = array();
                        while($rows = $results->fetchRow(DB_FETCHMODE_ASSOC)){
                            $contents .= $sep .$rows["SUBCLASSNAME"].",欠時数,遅・早数,欠課数";
                            $this->opt_subclass[] = $rows["SUBCLASSCD"];
                            $sep = ",";
                        }
                        break;
                    default:
                        $contents .= $sep .$item_array[$item[$i]];
                        break;
                }
            } else {
                switch($item[$i]){
                    case "KEKKA_INFO":
                        $results = $db->query(knji100c_03Query::getSubclassList($this, $attend_seme, $month));
                        $this->opt_subclass = array();
                        while($rows = $results->fetchRow(DB_FETCHMODE_ASSOC)){
                            $contents .= $sep .$rows["SUBCLASSNAME"].",欠時数,遅・早数,欠課数";
                            $this->opt_subclass[] = $rows["SUBCLASSCD"];
                            $sep = ",";
                        }
                        break;
                    default:
                        $contents .= $sep .$item_array[$item[$i]];
                        break;
                }
            }
            $sep = ",";
        }
        $contents .= "\n";

        // 遅刻何回で欠課とするかの指数取得
        $absent = array();
        $absent = $db->getRow(knji100c_03Query::getSchoolAbsent(), DB_FETCHMODE_ASSOC);
        $absent_cov      = $absent["ABSENT_COV"];
        $absent_cov_late = $absent["ABSENT_COV_LATE"];
        $amari_kuriage   = $absent["AMARI_KURIAGE"];

        //データ部作成
        $dataTemp = array();
        $tempCnt = array();
        $data = false;
        $result = $db->query(knji100c_03Query::selectQuery($this, $attend_seme, $month, $absent_cov, $absent_cov_late, $amari_kuriage));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $sep = "";
            for($i = 0; $i < get_count($item); $i++){
                if($item[$i] == "KEKKA_INFO") {
                    for($j=0; $j<get_count($this->opt_subclass); $j++){
				        //教育課程対応
				        if ($this->Properties["useCurriculumcd"] == '1') {
                        	if($row["SUBCLASSCD".$j]) {
		                        $contents .= $sep .$row["CLASSCD".$j].'-'.$row["SCHOOL_KIND".$j].'-'.$row["CURRICULUM_CD".$j].'-'.$row["SUBCLASSCD".$j].','.$row["KETSUJI".$j].','.$row["LATE_EARLY".$j].','.$row["KEKKA".$j];
                            } else {
	                        	$contents .= $sep .$row["SUBCLASSCD".$j].','.$row["KETSUJI".$j].','.$row["LATE_EARLY".$j].','.$row["KEKKA".$j];
                        	}
                        } else {
	                        $contents .= $sep .$row["SUBCLASSCD".$j].','.$row["KETSUJI".$j].','.$row["LATE_EARLY".$j].','.$row["KEKKA".$j];
                        }
                        $sep = ",";
                    }
                } else {
                    $contents .= $sep .$row[$item[$i]];
                }
                $sep = ",";
            }
            $contents .= "\n";
            $data = true;
        }
        Query::dbCheckIn($db);
        if ($data){
            $filename = sprintf("%s.csv","生徒基礎データ（出欠情報：欠課情報）");
            common::downloadContents($contents, $filename);
            exit;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
