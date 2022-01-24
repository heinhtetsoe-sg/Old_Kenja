<?php

require_once('for_php7.php');
class knji100Model extends Model {

    var $cmd;
    var $schregno;                  //学籍番号
    var $control_data = array();    //コントロールマスタデータを保持
    var $item;
    var $selectdata;                //書出し項目一覧
    var $mode;                      //生徒区分（在校生・卒業生）
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    function knja150Model()
    {
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

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
                            "FINSCHOOLCD"               => "出身学校(*)",
                            "FINISH_DATE"               => "出身校卒業日付",
                            "PRISCHOOLCD"               => "塾(*)",
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
                            "ATTENDNO"                  => "卒業時出席番号",
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
        $this->selectdata   = VARS::post("selectdata");
        //学籍番号
        $this->schregno     = VARS::post("SCHREGNO");
        //出力設定
        $this->output       = VARS::post("OUTPUT");
        //生徒区分
        $this->mode         = VARS::post("mode");
        if (VARS::get("PROGRAMID") || VARS::get("ini")) {
            unset($this->schregno);
        }
    }

    function &getProgrampathModel()
    {
        $db = Query::dbCheckOut();
        $programpath = $db->getOne(knji100Query::getProgrampath());
        Query::dbCheckIn($db);

        return $programpath;
    }

    //CSV書出し
    function &getCsvModel()
    {
        $db           = Query::dbCheckOut();
        if($this->mode == "ungrd"){
            $result       = $db->query(knji100Query::selectUngrdQuery($this));
        }elseif($this->mode == "grd"){
            $result       = $db->query(knji100Query::selectGrdQuery($this));
        }
        $item = explode(",", $this->selectdata);
        $contents = $sep = "";
        //ヘッダ作成
        for($i = 0; $i < get_count($item); $i++){

            if ($this->output == OUT_CODE_NAME){    //コード＋名称
                switch($item[$i]){
                    case "INOUTCD":
                    case "SEX":
                    case "FINSCHOOLCD":
                    case "PRISCHOOLCD":
                    case "ENT_DIV":
                    case "GRD_DIV":
                    case "AREACD":
                    case "RELATIONSHIP":
                    case "GUARD_SEX":
                    case "GUARD_JOBCD":
                    case "GUARANTOR_RELATIONSHIP":
                    case "GUARANTOR_SEX":
                    case "GUARANTOR_JOBCD":
                        $contents .= $sep ."コード,".$this->item[$item[$i]];
                        break;
                    default:
                        $contents .= $sep .$this->item[$item[$i]];
                        break;
                }
            } else {
                $contents .= $sep .$this->item[$item[$i]];
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