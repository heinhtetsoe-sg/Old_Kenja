<?php

require_once('for_php7.php');
class knji100c_06Model extends Model {

    var $cmd;
    var $schregno;                  //学籍番号
    var $control_data = array();    //コントロールマスタデータを保持
    var $item;
    var $selectdata_l;              //対象生徒
    var $selectdata_r;              //書出し項目一覧

    function knja150Model() {
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init() {
        $this->cmd = VARS::request("cmd");
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
        /**********************************/
        /************ 選択項目 ************/
        /**********************************/
        $this->item = array();
        $this->item = $this->getItemArray();
        /**********************************/
        /************ パラメータ **********/
        /**********************************/
        //サブシステム
        $this->subsystem = (VARS::get("SUBSYSTEM")) ? VARS::get("SUBSYSTEM") : '06';
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

        $this->getPropertiesAll();
        if (VARS::get("SEND_selectSchoolKind")) {
            $this->selectSchoolKind = VARS::get("SEND_selectSchoolKind");
        }
    }

    /**********************************/
    /************ 選択項目 ************/
    /**********************************/
    function getItemArray() {
        $item = array();

        $item[]["SINGAKU_PASS"]   = "進学（合格）";
        $item[]["SINGAKU_LAST"]   = "進学（最終進路）";
        $item[]["SINGAKU_ALL"]    = "進学（全て）";
        $item[]["SHUSHOKU_PASS"]  = "就職（合格）";
        $item[]["SHUSHOKU_ALL"]   = "就職（全て）";

        return $item;
    }

    //CSV書出し
    function &getCsvModel() {
        $db           = Query::dbCheckOut();
        $item = explode(",", $this->selectdata_r);

        $query = knji100c_06Query::getDelFieldSql($this);
        $db->query($query);
        $setSeq = 1;
        foreach ($item as $key => $val) {
            $query = knji100c_06Query::getFieldInsSql($this, $setSeq, $val);
            $db->query($query);
            $setSeq++;
        }

        $contents = $query = "";
        $header = array();
        $code = ($this->output == OUT_CODE_NAME) ? "コード," : "";
        //ヘッダ(進学)
        //ヘッダ(就職)
        //ヘッダ作成
        for ($i = 0; $i < get_count($item); $i++) {
            switch($item[$i]){
                case "SINGAKU_PASS":
                case "SINGAKU_LAST":
                case "SINGAKU_ALL":
                    $header = array("SCHREGNO"          => "学籍番号",
                                    "NAME"              => "生徒氏名",
                                    "GRADE"             => "学年",
                                    "HR_CLASS"          => "組",
                                    "ATTENDNO"          => "出席番号",
                                    "SEX"               => $code."性別",
                                    "SCHOOL_GROUP_NAME" => $code."学校系列",
                                    "PREF_NAME"         => $code."所在地",
                                    "STAT_NAME"         => $code."学校名",
                                    "FACULTYNAME"       => $code."学部",
                                    "DEPARTMENTNAME"    => $code."学科",
                                    "HOWTOEXAM_NAME"    => $code."受験方式",
                                    "DECISION_NAME"     => $code."合否",
                                    "PLANSTAT_NAME"     => $code."進路先"
                                    );

                    $query = knji100c_06Query::getPrintSingaku($this, $item[$i]);

                    //コードに対象項目の名称を追加　(例) コード　→　性別コード
                    if ($this->output == OUT_CODE_NAME) {
                        foreach ($header as $key => $value) {
                            if (mb_strpos($value, $code) !== false) {
                                $prefix = str_replace($code, "", $value);
                                $header[$key] = $prefix.$value;
                            }
                        }
                    }
                    break;
                case "SHUSHOKU_PASS":
                case "SHUSHOKU_ALL":
                    $header = array("SCHREGNO"          => "学籍番号",
                                    "NAME"              => "生徒氏名",
                                    "GRADE"             => "学年",
                                    "HR_CLASS"          => "組",
                                    "ATTENDNO"          => "出席番号",
                                    "SEX"               => $code."性別",
                                    "STAT_NAME"         => $code."会社名",
                                    "INDUSTRY_LNAME"    => $code."産業種別",
                                    "PREF_NAME"         => $code."就業場所",
                                    "HOWTOEXAM_NAME"    => $code."応募方法",
                                    "DECISION_NAME"     => $code."合否",
                                    "PLANSTAT_NAME"     => $code."進路先"
                                    );
                    $query = knji100c_06Query::getPrintShushoku($this, $item[$i]);

                    if ($this->output == OUT_CODE_NAME) {
                        foreach ($header as $key => $value) {
                            if (mb_strpos($value, $code) !== false) {
                                $prefix = str_replace($code, "", $value);
                                $header[$key] = $prefix.$value;
                            }
                        }
                    }

                    break;
                default:
                    $contents .= "\n";
                    break;
            }
        }
        $contents .= implode($header, ",") ."\n";
        //データ部作成
        $data = false;
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //$row = str_replace(",", ".", $row);
            $sep = "";
            foreach ($header as $field => $name) {
                $contents .= $sep .$row[$field];
                $sep = ",";
            }
            $contents .= "\n";
            $data = true;
        }
        Query::dbCheckIn($db);
        if ($data){
            $filename = sprintf("%s.csv","生徒基礎データ（進路情報）");
            common::downloadContents($contents, $filename);
            exit;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>