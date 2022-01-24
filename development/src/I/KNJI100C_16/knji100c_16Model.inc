<?php

require_once('for_php7.php');

class knji100c_16Model extends Model {

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

        //ITEM
        $this->selectdata_r     = VARS::post("selectdata_r");
        //対象生徒
        $this->selectdata_l     = VARS::post("selectdata_l");
        //学年
        $this->grade            = VARS::post("GRADE");
        //年組
        $this->grade_hr_class   = VARS::post("GRADE_HR_CLASS");
        //出力設定
        $this->output           = VARS::post("OUTPUT");

        //サブシステム
        $this->subsystem = (VARS::get("SUBSYSTEM")) ? VARS::get("SUBSYSTEM") : '16';
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

        $this->item1 = array("SCHREGNO"         => "学籍番号",
                             "NAME"             => "生徒氏名",
                             "GRADE"            => "学年",
                             "HR_CLASS"         => "組",
                             "ATTENDNO"         => "出席番号",
                            );
                            
        if ($this->output == 1){    //コード＋名称
            $this->item2 = array("TRAINDATE"       => "指導日付",
                                "PATIENTCD"        => "相談者コード",
                                "PATIENT"          => "相談者",
                                "STAFFCD"          => "対応者コード",
                                "STAFFNAME_SHOW"   => "対応者",
                                "HOWTOTRAINCD"     => "指導方法コード",
                                "HOWTOTRAIN"       => "指導方法",
                                "CONTENT"          => "指導内容",
                                );
        }else{
            $this->item2 = array("TRAINDATE"       => "指導日付",
                                "PATIENT"          => "相談者",
                                "STAFFNAME_SHOW"   => "対応者",
                                "HOWTOTRAIN"       => "指導方法",
                                "CONTENT"          => "指導内容",
                                );
        }
        
    }

    //CSV書出し
    function &getCsvModel() {
        $db           = Query::dbCheckOut();
        $item = explode(",", $this->selectdata_r);

        $query = knji100c_16Query::getDelFieldSql($this);
        $db->query($query);
        $setSeq = 1;
        foreach ($item as $key => $val) {
            $query = knji100c_16Query::getFieldInsSql($this, $setSeq, $val);
            $db->query($query);
            $setSeq++;
        }

        $result       = $db->query(knji100c_16Query::selectQuery($this));
        $contents = $sep = "";
        $data = false;

        //出力項目
        foreach($this->item1 as $key => $val) $item1[] = $key;
        foreach($this->item2 as $key => $val) $item2[] = $key;

        //ヘッダ作成（生徒情報）
        for($i = 0; $i < sizeof($item1); $i++) {
            
            if ($this->output == 1){    //コード＋名称
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
        //ヘッダ作成（個人指導情報）
        $hist_cnt = $db->getOne(knji100c_16Query::getHistCount($this));
        $hist_cnt = ($hist_cnt > 1) ? $hist_cnt : 1;
        for($h = 0; $h < $hist_cnt; $h++) {
            for($i = 0; $i < sizeof($item2); $i++) {
                $contents .= $sep .$this->item2[$item2[$i]];
                $sep = ",";
            }
        }
        $contents .= "\n";


        //データ部作成
        $schregno = "";
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(","   , "." , $row);  //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace("\r\n", "↓", $row);
            $row = str_replace("\r"  , "↓", $row);
            $row = str_replace("\n"  , "↓", $row);

            if($schregno != $row["SCHREGNO"]) {
                $sep = "";
                if($data) $contents .= "\n";
                //生徒情報
                for($i = 0; $i < sizeof($item1); $i++){
                    $contents .= $sep .$row[$item1[$i]];
                    $sep = ",";
                }
            }
            //個人指導情報
            //個人指導情報出力 ここから
            for($i = 0; $i < sizeof($item2); $i++){
                $contents .= $sep .$row[$item2[$i]];
                $sep = ",";
            }
            //個人指導情報出力 ここまで

            $schregno = $row["SCHREGNO"];
            $data = true;


        }
        Query::dbCheckIn($db);
        if ($data){
            $filename = sprintf("%s.csv","生徒基礎データ（個人指導情報）");
            common::downloadContents($contents, $filename);
            exit;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>