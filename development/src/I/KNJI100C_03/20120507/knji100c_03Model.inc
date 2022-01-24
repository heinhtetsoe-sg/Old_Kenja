<?php
class knji100c_03Model extends Model {

    var $cmd;
    var $schregno;                  //学籍番号
    var $control_data = array();    //コントロールマスタデータを保持
    var $item;
    var $selectdata_l;              //対象生徒
    var $selectdata_r;              //書出し項目一覧
    var $opt_subclass  = array();

    function knja150Model() {
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->item = array("SCHREGNO"                  => "学籍番号",
                            "NAME"                      => "生徒氏名",
                            "GRADE"                     => "学年",
                            "HR_CLASS"                  => "組",
                            "ATTENDNO"                  => "出席番号",
                            "SEX"                       => "性別区分(*)",
                            "KEKKA_INFO"                => "欠課情報"
                            );
        //ITEM
        $this->selectdata_r     = VARS::post("selectdata_r");
        //対象生徒
        $this->selectdata_l     = VARS::post("selectdata_l");
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
        //年組
        if(VARS::get("GRADE_HR_CLASS"))
            $this->grade_hr_class   = VARS::get("GRADE_HR_CLASS");
        //出力設定
        if(VARS::get("OUTPUT"))
            $this->output           = VARS::get("OUTPUT");
    }

    //CSV書出し
    function &getCsvModel() {
        $db = Query::dbCheckOut();
        $item = explode(",", $this->selectdata_r);

        $contents = $sep = "";
        //ヘッダ作成
        for($i = 0; $i < sizeof($item); $i++){

            if ($this->output == OUT_CODE_NAME){    //コード＋名称
                switch($item[$i]){
                    case "SEX":
                        $contents .= $sep ."コード,".$this->item[$item[$i]];
                        break;
                    case "KEKKA_INFO":
                        $results = $db->query(knji100c_03Query::getSubclassList($this));
                        $this->opt_subclass = array();
                        while($rows = $results->fetchRow(DB_FETCHMODE_ASSOC)){
                            $contents .= $sep .$rows["SUBCLASSNAME"].",欠時数,遅・早数,欠課数";
                            $this->opt_subclass[] = $rows["SUBCLASSCD"];
                            $sep = ",";
                        }
                        break;
                    default:
                        $contents .= $sep .$this->item[$item[$i]];
                        break;
                }
            } else {
                switch($item[$i]){
                    case "KEKKA_INFO":
                        $results = $db->query(knji100c_03Query::getSubclassList($this));
                        $this->opt_subclass = array();
                        while($rows = $results->fetchRow(DB_FETCHMODE_ASSOC)){
                            $contents .= $sep .$rows["SUBCLASSNAME"].",欠時数,遅・早数,欠課数";
                            $this->opt_subclass[] = $rows["SUBCLASSCD"];
                            $sep = ",";
                        }
                        break;
                    default:
                        $contents .= $sep .$this->item[$item[$i]];
                        break;
                }
            }
            $sep = ",";
        }
        $contents .= "\n";

        //データ部作成
        $dataTemp = array();
        $tempCnt = array();
        $result = $db->query(knji100c_03Query::selectQuery($this));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $sep = "";
            for($i = 0; $i < sizeof($item); $i++){
                if($item[$i] == "KEKKA_INFO") {
                    for($j=0; $j<count($this->opt_subclass); $j++){
                        $contents .= $sep .$row["SUBCLASSCD".$j].','.$row["KETSUJI".$j].','.$row["LATE_EARLY".$j].','.$row["KEKKA".$j];
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