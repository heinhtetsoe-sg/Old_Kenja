<?php

require_once('for_php7.php');

class knji100aModel extends Model {

    var $cmd;
    var $schregno;                  //学籍番号
    var $control_data = array();    //コントロールマスタデータを保持
    var $item;
    var $selectdata_l;                //対象生徒
    var $selectdata_r;                //書出し項目一覧
    var $mode;                      //生徒区分（在校生・卒業生）
    var $field;

    function knja150Model() {
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init() {
        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル

        $addr1Title = $this->Properties["useAddrField2"] == "1" ? "１" : "";
        $this->item = array("SCHREGNO"                  => "学籍番号",
                            "INOUTCD"                   => "内外区分(*)",
                            "NAME"                      => "生徒氏名",
                            "NAME_SHOW"                 => "生徒氏名表示用",
                            "NAME_KANA"                 => "生徒氏名かな",
                            "NAME_ENG"                  => "生徒氏名英字",
                            "REAL_NAME"                 => "生徒戸籍氏名",
                            "REAL_NAME_KANA"            => "生徒戸籍氏名かな",
                            "BIRTHDAY"                  => "生年月日",
                            "SEX"                       => "性別区分(*)",
                            "BLOODTYPE"                 => "血液型",
                            "BLOOD_RH"                  => "血液型(RH)",
                            "PRISCHOOLCD"               => "塾(*)",
                            "FINSCHOOLCD"               => "出身学校(*)",
                            "FINISH_DATE"               => "出身校卒業日付",
                            "CURRICULUM_YEAR"           => "課程入学年度",
                            "ENT_DATE"                  => "入学日付",
                            "ENT_DIV"                   => "入学区分(*)",
                            "ENT_REASON"                => "入学事由",
                            "ENT_SCHOOL"                => "入学学校名",
                            "ENT_ADDR"                  => "入学学校住所".$addr1Title,
                            "ENT_ADDR2"                 => "入学学校住所２",
                            "GRD_DATE"                  => "卒業(除籍)日付",
                            "GRD_DIV"                   => "卒業(除籍)区分(*)",
                            "GRD_REASON"                => "卒業(除籍)事由",
                            "GRD_SCHOOL"                => "卒業(除籍)学校名",
                            "GRD_ADDR"                  => "卒業(除籍)学校住所".$addr1Title,
                            "GRD_ADDR2"                 => "卒業(除籍)学校住所２",
                            "GRD_NO"                    => "卒業生台帳番号",
                            "GRD_TERM"                  => "卒業期",
                            "REMARK1"                   => "備考１",
                            "REMARK2"                   => "備考２",
                            "REMARK3"                   => "備考３",
                            "GRADE"                     => "学年",
                            "HR_CLASS"                  => "組",
                            "ATTENDNO"                  => "出席番号",
                            "ANNUAL"                    => "年次",
                            "COURSECD"                  => "課程",
                            "MAJORCD"                   => "学科",
                            "COURSECODE"                => "コース",
                            "STAFFNAME"                 => "担任",
                            "ZIPCD"                     => "現住所郵便番号",
                            "AREACD"                    => "地区(*)",
                            "ADDR1"                     => "現住所１",
                            "ADDR2"                     => "現住所２",
                            "ADDR1_ENG"                 => "現住所１(英字)",
                            "ADDR2_ENG"                 => "現住所２(英字)",
                            "TELNO"                     => "現住所電話番号",
                            "FAXNO"                     => "現住所FAX番号",
                            "EMAIL"                     => "現住所E-MAILアドレス",
                            "EMERGENCYCALL"             => "急用連絡先",
                            "EMERGENCYNAME"             => "急用連絡先名",
                            "EMERGENCYRELA_NAME"        => "急用連絡先続柄",
                            "EMERGENCYTELNO"            => "急用電話番号",
                            "EMERGENCYCALL2"            => "急用連絡先２",
                            "EMERGENCYNAME2"            => "急用連絡先名２",
                            "EMERGENCYRELA_NAME2"       => "急用連絡先続柄２",
                            "EMERGENCYTELNO2"           => "急用電話番号２",
                            "RELATIONSHIP"              => "保護者との続柄(*)",
                            "GUARD_NAME"                => "保護者氏名",
                            "GUARD_KANA"                => "保護者氏名かな",
                            "GUARD_SEX"                 => "保護者性別区分(*)",
                            "GUARD_BIRTHDAY"            => "保護者生年月日",
                            "GUARD_ZIPCD"               => "保護者郵便番号",
                            "GUARD_ADDR1"               => "保護者住所１",
                            "GUARD_ADDR2"               => "保護者住所２",
                            "GUARD_TELNO"               => "保護者電話番号",
                            "GUARD_FAXNO"               => "保護者FAX番号",
                            "GUARD_E_MAIL"              => "保護者E-MAILアドレス",
                            "GUARD_JOBCD"               => "保護者職種(*)",
                            "GUARD_WORK_NAME"           => "保護者勤務先名称",
                            "GUARD_WORK_TELNO"          => "保護者勤務先電話番号",
                            "GUARANTOR_RELATIONSHIP"    => "保証人との続柄(*)",
                            "GUARANTOR_NAME"            => "保証人氏名",
                            "GUARANTOR_KANA"            => "保証人氏名かな",
                            "GUARANTOR_SEX"             => "保証人性別区分(*)",
                            "GUARANTOR_ZIPCD"           => "保証人郵便番号",
                            "GUARANTOR_ADDR1"           => "保証人住所１",
                            "GUARANTOR_ADDR2"           => "保証人住所２",
                            "GUARANTOR_TELNO"           => "保証人電話番号",
                            "GUARANTOR_JOBCD"           => "保証人職種(*)",
                            "PUBLIC_OFFICE"             => "兼ねている公職"
                            );
        if ($this->Properties["useAddrField2"] != "1") {
            unset($this->item['ENT_ADDR2']);
            unset($this->item['GRD_ADDR2']);
        }
        //ITEM
        $this->selectdata_r   = VARS::post("selectdata_r");
        //対象生徒
        $this->selectdata_l   = VARS::post("selectdata_l");
        //年組
        $this->grade_hr_class = VARS::post("GRADE_HR_CLASS");
        //異動対象日付
        $this->date           = VARS::post("DATE");
        //出力設定
        $this->output         = VARS::post("OUTPUT");

        $this->field = array("CTRL_YEAR"        =>  VARS::post("CTRL_YEAR"),
                             "CTRL_SEMESTER"    =>  VARS::post("CTRL_SEMESTER"),
                             "CTRL_DATE"        =>  VARS::post("CTRL_DATE"),
                             "KAKIDASHI_UP"     =>  VARS::post("KAKIDASHI_UP"));
    }

    //CSV書出し
    function &getCsvModel() {
        $db           = Query::dbCheckOut();
        $result       = $db->query(knji100aQuery::selectUngrdQuery($this));
        $item = explode(",", $this->selectdata_r);
        $contents = $sep = "";
        $schoolkind = $db->getCol(knji100aQuery::getSchoolKind());
        //ヘッダ作成
        for($i = 0; $i < sizeof($item); $i++){

            if ($this->output == OUT_CODE_NAME){    //コード＋名称
                switch($item[$i]){
                    case "INOUTCD":
                    case "SEX":
                    case "PRISCHOOLCD":
                    case "AREACD":
                    case "RELATIONSHIP":
                    case "GUARD_SEX":
                    case "GUARD_JOBCD":
                    case "GUARANTOR_RELATIONSHIP":
                    case "GUARANTOR_SEX":
                    case "GUARANTOR_JOBCD":
                        $contents .= $sep ."コード,".$this->item[$item[$i]];
                        break;
                    case "ENT_DIV":
                    case "GRD_DIV":
                        for($j = 0; $j < get_count($schoolkind); $j++){
                            $contents .= $sep ."コード,".$schoolkind[$j].$this->item[$item[$i]];
                            $sep = ",";
                        }
                        break;
                    case "FINSCHOOLCD":
                        for($j = 0; $j < get_count($schoolkind); $j++){
                            $fin_schoolkind = "";
                            switch ($schoolkind[$j]) {
                                case "P":
                                    $fin_schoolkind = 'K';
                                    break;
                                case "J":
                                    $fin_schoolkind = 'P';
                                    break;
                                case "H":
                                    $fin_schoolkind = 'J';
                                    break;
                            }
                            $contents .= $sep ."コード,".$fin_schoolkind.$this->item[$item[$i]];
                            $sep = ",";
                        }
                        break;
                    case "CURRICULUM_YEAR":
                        for($j = 0; $j < get_count($schoolkind); $j++){
                            $contents .= $sep .$schoolkind[$j].$this->item[$item[$i]];
                            $sep = ",";
                        }
                        break;
                    case "ENT_DATE":
                    case "ENT_REASON":
                    case "ENT_SCHOOL":
                    case "ENT_ADDR":
                    case "ENT_ADDR2":
                    case "GRD_DATE":
                    case "GRD_REASON":
                    case "GRD_SCHOOL":
                    case "GRD_ADDR":
                    case "GRD_ADDR2":
                    case "GRD_NO":
                    case "GRD_TERM":
                        for($j = 0; $j < get_count($schoolkind); $j++){
                            $contents .= $sep .$schoolkind[$j].$this->item[$item[$i]];
                            $sep = ",";
                        }
                        break;
                    case "FINISH_DATE":
                        for($j = 0; $j < get_count($schoolkind); $j++){
                            $fin_schoolkind = "";
                            switch ($schoolkind[$j]) {
                                case "P":
                                    $fin_schoolkind = 'K';
                                    break;
                                case "J":
                                    $fin_schoolkind = 'P';
                                    break;
                                case "H":
                                    $fin_schoolkind = 'J';
                                    break;
                            }
                            $contents .= $sep .$fin_schoolkind.$this->item[$item[$i]];
                            $sep = ",";
                        }
                        break;
                    default:
                        $contents .= $sep .$this->item[$item[$i]];
                        break;
                }
            } else {
                switch($item[$i]){
                    case "CURRICULUM_YEAR":
                        for($j = 0; $j < get_count($schoolkind); $j++){
                            $contents .= $sep .$schoolkind[$j].$this->item[$item[$i]];
                            $sep = ",";
                        }
                        break;
                    case "ENT_DATE":
                    case "ENT_DIV":
                    case "ENT_REASON":
                    case "ENT_SCHOOL":
                    case "ENT_ADDR":
                    case "ENT_ADDR2":
                    case "GRD_DATE":
                    case "GRD_DIV":
                    case "GRD_REASON":
                    case "GRD_SCHOOL":
                    case "GRD_ADDR":
                    case "GRD_ADDR2":
                    case "GRD_NO":
                    case "GRD_TERM":
                        for($j = 0; $j < get_count($schoolkind); $j++){
                            $contents .= $sep .$schoolkind[$j].$this->item[$item[$i]];
                            $sep = ",";
                        }
                        break;
                    case "FINSCHOOLCD":
                    case "FINISH_DATE":
                        for($j = 0; $j < get_count($schoolkind); $j++){
                            $fin_schoolkind = "";
                            switch ($schoolkind[$j]) {
                                case "P":
                                    $fin_schoolkind = 'K';
                                    break;
                                case "J":
                                    $fin_schoolkind = 'P';
                                    break;
                                case "H":
                                    $fin_schoolkind = 'J';
                                    break;
                            }
                            $contents .= $sep .$fin_schoolkind.$this->item[$item[$i]];
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
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $sep = "";
            for($i = 0; $i < sizeof($item); $i++){
                switch($item[$i]){
                    case "FINISH_DATE":
                    case "CURRICULUM_YEAR":
                        for($j = 0; $j < get_count($schoolkind); $j++){
                            $hist_data = $db->getOne(knji100aQuery::getSchregEntGrdHistDat($item[$i], $row["SCHREGNO"], $schoolkind[$j]));
                            $contents .= $sep .$hist_data;
                            $sep = ",";
                        }
                        break;
                    case "ENT_DATE":
                    case "ENT_REASON":
                    case "ENT_SCHOOL":
                    case "ENT_ADDR":
                    case "ENT_ADDR2":
                    case "GRD_DATE":
                    case "GRD_REASON":
                    case "GRD_SCHOOL":
                    case "GRD_ADDR":
                    case "GRD_ADDR2":
                    case "GRD_NO":
                    case "GRD_TERM":
                        for($j = 0; $j < get_count($schoolkind); $j++){
                            $hist_data = $db->getOne(knji100aQuery::getSchregEntGrdHistDat($item[$i], $row["SCHREGNO"], $schoolkind[$j]));
                            $contents .= $sep .$hist_data;
                            $sep = ",";
                        }
                        break;
                    case "FINSCHOOLCD":
                        for($j = 0; $j < get_count($schoolkind); $j++){
                            $hist_data = $db->getOne(knji100aQuery::getSchregEntGrdHistDat($item[$i], $row["SCHREGNO"], $schoolkind[$j]));
                            if (!$hist_data && $this->output == OUT_CODE_NAME) {
                                $chg_data = ",";
                            } else {
                                $chg_data = $db->getOne(knji100aQuery::getFinSchoolMst($this, $hist_data));
                                //出身学校マスタに登録されていないコードの場合、","を付与する
                                if(!$chg_data) {
                                    $chg_data = ",";
                                }
                            }
                            $contents .= $sep .$chg_data;
                            $sep = ",";
                        }
                        break;
                    case "ENT_DIV":
                    case "GRD_DIV":
                        $namecd1 = ($item[$i] == "ENT_DIV") ? 'A002' : 'A003';
                        for($j = 0; $j < get_count($schoolkind); $j++){
                            $hist_data = $db->getOne(knji100aQuery::getSchregEntGrdHistDat($item[$i], $row["SCHREGNO"], $schoolkind[$j]));
                            $chg_data  = (!$hist_data && $this->output == OUT_CODE_NAME) ? "," : $db->getOne(knji100aQuery::getNameMst($this, $namecd1, $hist_data));
                            $contents .= $sep .$chg_data;
                            $sep = ",";
                        }
                        break;
                    default:
                        $contents .= $sep .$row[$item[$i]];
                        break;
                }
                $sep = ",";
            }
            $contents .= "\n";
            $data = true;
        }
        //書出し
        if ($data) {
            if ($this->field["KAKIDASHI_UP"] == "1") {
                knji100aQuery::getUpdateKakidashi($db, $this);
            }
        }
        Query::dbCheckIn($db);
        if ($data){
            $filename = sprintf("%s.csv","生徒基礎データ");
            common::downloadContents($contents, $filename);
            exit;
        }else{
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>