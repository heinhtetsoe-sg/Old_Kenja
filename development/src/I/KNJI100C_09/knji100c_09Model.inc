<?php

require_once('for_php7.php');
class knji100c_09Model extends Model {

    var $cmd;
    var $schregno;                  //学籍番号
    var $control_data = array();    //コントロールマスタデータを保持
    var $item1;
    var $item2;
    var $selectdata_l;              //対象生徒
    var $selectdata_r;              //書出し項目一覧

    function knja150Model() {
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->item1 = array("SCHREGNO"         => "学籍番号",
                             "NAME"             => "生徒氏名",
                             "GRADE"            => "学年",
                             "HR_CLASS"         => "組",
                             "ATTENDNO"         => "出席番号",
                             "SEX"              => "性別区分(*)",
                            );

        $this->item2 = array("CLUBCD"           => "部クラブ(*)",
                             "SDATE"            => "入部日付",
                             "EDATE"            => "退部日付",
                             "EXECUTIVECD"      => "役職(*)",
                             "REMARK"           => "備考",
                            );
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

        //サブシステム
        $this->subsystem = (VARS::get("SUBSYSTEM")) ? VARS::get("SUBSYSTEM") : '09';
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

    //CSV書出し
    function &getCsvModel() {
        $db           = Query::dbCheckOut();
        $item = explode(",", $this->selectdata_r);

        $query = knji100c_09Query::getDelFieldSql($this);
        $db->query($query);
        $setSeq = 1;
        foreach ($item as $key => $val) {
            $query = knji100c_09Query::getFieldInsSql($this, $setSeq, $val);
            $db->query($query);
            $setSeq++;
        }

        $result       = $db->query(knji100c_09Query::selectQuery($this));
        $contents = $sep = "";
        $data = false;

        //出力項目
        foreach($this->item1 as $key => $val) $item1[] = $key;
        foreach($this->item2 as $key => $val) $item2[] = $key;

        //ヘッダ作成（生徒情報）
        for($i = 0; $i < get_count($item1); $i++) {
            if ($this->output == OUT_CODE_NAME){    //コード＋名称
                switch($item1[$i]){
                    case "SEX":
                        $code = str_replace("(*)", "", $this->item1[$item1[$i]]);
                        $code .= "コード,";
                        $contents .= $sep .$code.$this->item1[$item1[$i]];
                        break;
                    default:
                        $contents .= $sep .$this->item1[$item1[$i]];
                        break;
                }
            } else {
                $contents .= $sep .$this->item1[$item1[$i]];
            }
            $sep = ",";
        }
        //ヘッダ作成（部活動）
        $hist_cnt = $db->getOne(knji100c_09Query::getClubHistCount($this));
        $hist_cnt = ($hist_cnt > 1) ? $hist_cnt : 1;
        for($h = 0; $h < $hist_cnt; $h++) {
            for($i = 0; $i < get_count($item2); $i++) {
                if ($this->output == OUT_CODE_NAME){    //コード＋名称
                    switch($item2[$i]){
                        case "CLUBCD":
                        case "EXECUTIVECD":
                            $code = str_replace("(*)", "", $this->item2[$item2[$i]]);
                            $code .= "コード,";
                            $contents .= $sep .$code.$this->item2[$item2[$i]];
                            break;
                        default:
                            $contents .= $sep .$this->item2[$item2[$i]];
                            break;
                    }
                } else {
                    $contents .= $sep .$this->item2[$item2[$i]];
                }
                $sep = ",";
            }
        }
        $contents .= "\n";

        //データ部作成
        $schregno = "";
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            if($schregno != $row["SCHREGNO"]) {
                $sep = "";
                if($data) $contents .= "\n";
                //生徒情報
                for($i = 0; $i < get_count($item1); $i++){
                    $contents .= $sep .$row[$item1[$i]];
                    $sep = ",";
                }
            }
            //部活動情報
            for($i = 0; $i < get_count($item2); $i++){
                $contents .= $sep .$row[$item2[$i]];
                $sep = ",";
            }
            $schregno = $row["SCHREGNO"];
            $data = true;
        }
        Query::dbCheckIn($db);
        if ($data){
            $filename = sprintf("%s.csv","生徒基礎データ（部活動情報）");
            common::downloadContents($contents, $filename);
            exit;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>