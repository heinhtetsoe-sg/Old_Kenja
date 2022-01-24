<?php

require_once('for_php7.php');
class knji100c_02Model extends Model {

    var $cmd;
    var $schregno;                  //学籍番号
    var $control_data = array();    //コントロールマスタデータを保持
    var $item;
    var $selectdata_l;              //対象生徒
    var $selectdata_r;              //書出し項目一覧
    var $koteidata;                 //CSV項目(固定)
    var $virus;
    var $koudome;

    function knja150Model() {
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
        $this->item[]["LESSON"]         = "授業日数";
        $this->item[]["OFFDAYS"]        = "休学日数";
        $this->item[]["ABSENT"]         = "公欠日数";
        $this->item[]["SUSPEND"]        = "出停日数";
        $this->item[]["MOURNING"]       = "忌引日数";
        $this->item[]["ABROAD"]         = "留学日数";
        $this->item[]["SICK"]           = "4-病欠日数";
        $this->item[]["NOTICE"]         = "5-事故欠届日数";
        $this->item[]["NONOTICE"]       = "6-事故欠無日数";
        $this->item[]["LATE"]           = "遅刻日数";
        $this->item[]["EARLY"]          = "早退日数";

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
        $this->subsystem = (VARS::get("SUBSYSTEM")) ? VARS::get("SUBSYSTEM") : '02';
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
            
        $this->getPropertiesAll(); //使用するテーブルを取得
        if (VARS::get("SEND_selectSchoolKind")) {
            $this->selectSchoolKind = VARS::get("SEND_selectSchoolKind");
        }

        $this->virus     = ($this->Properties["useVirus"] == 'true') ? true : false;
        $this->koudome   = ($this->Properties["useKoudome"] == 'true') ? true : false;
    }

    //CSV書出し
    function &getCsvModel() {
        $db           = Query::dbCheckOut();
        $result       = $db->query(knji100c_02Query::selectQuery($this));
        $this->koteidata = "";
        foreach($this->kotei as $key => $val) {
            foreach ($val as $field => $label) {
                $this->koteidata = $this->koteidata.$field.",";
            }
        }
        $item = explode(",", $this->selectdata_r);

        $query = knji100c_02Query::getDelFieldSql($this);
        $db->query($query);
        $setSeq = 1;
        foreach ($item as $key => $val) {
            $query = knji100c_02Query::getFieldInsSql($this, $setSeq, $val);
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
        //ヘッダ作成
        //固定項目+画面からの選択項目
        for($i = 0; $i < get_count($item); $i++){
            if ($this->output == OUT_CODE_NAME){    //コード＋名称
                $codePrefix = str_replace("(*)", "", $item_array[$item[$i]]);
                switch($item[$i]){
                    case "SEX":
                        $contents .= $sep .$codePrefix."コード,".$item_array[$item[$i]];
                        break;
                    case "SICK":
                    case "NOTICE":
                    case "NONOTICE":
                        list($cd, $name) = explode('-', $item_array[$item[$i]]);
                        $attendname = $db->getOne(knji100c_02Query::getAttendName($cd));
                        $attendname = ($attendname) ? $attendname.'時数' : $name;
                        $contents .= $sep .$attendname;
                        break;
                    default:
                        $contents .= $sep .$item_array[$item[$i]];
                        break;
                }
            } else {
                switch($item[$i]){
                    case "SICK":
                    case "NOTICE":
                    case "NONOTICE":
                        list($cd, $name) = explode('-', $item_array[$item[$i]]);
                        $attendname = $db->getOne(knji100c_02Query::getAttendName($cd));
                        $attendname = ($attendname) ? $attendname.'時数' : $name;
                        $contents .= $sep .$attendname;
                        break;
                    default:
                        $contents .= $sep .$item_array[$item[$i]];
                        break;
                }
            }
            $sep = ",";
        }
        $contents .= "\n";
        //データ部作成
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $sep = "";
            for($i = 0; $i < get_count($item); $i++){
                $contents .= $sep .$row[$item[$i]];
                $sep = ",";
            }
            $contents .= "\n";
            $data = true;
        }
        Query::dbCheckIn($db);
        if ($data){
            $filename = sprintf("%s.csv","生徒基礎データ（出欠情報：１日出欠情報）");
            common::downloadContents($contents, $filename);
            exit;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>