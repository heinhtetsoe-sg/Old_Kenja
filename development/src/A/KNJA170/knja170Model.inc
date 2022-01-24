<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja170Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限
    var $selectdata;

    /**
     * メンバ変数の初期化
     */
    function knja170Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
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

        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //学期
                             "FORM"             =>  VARS::post("FORM"),             //帳票パターン
                             "TEL"              =>  VARS::post("TEL"),              //電話番号出力
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS")    //年組
                     );

        $this->selectdata = VARS::post("selectdata");

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    function getDownloadModel()
    {
        //CSVデータ抽出処理
        //DB接続
        $db = Query::dbCheckOut();

        //ヘッダ項目（出力対象項目）
        $header = array();
        $header[1] = array( "HR_NAME"           => "年組",
                            "STAFFNAME_SHOW"    => "担任名",
                            "SCHREGNO"          => "生徒手帳番号",
                            "ATTENDNO"          => "学級番号",
                            "NAME"              => "氏名",
                            "NAME_KANA"         => "ふりがな",
                            "BIRTHDAY"          => "生年月日",
                            "ZIPCD"             => "郵便番号",
                            "ADDR1"             => "現住所",
                            "TEL"               => "電話番号",
                            "GUARD_NAME"        => "保護者氏名",
                            "E_TEL"             => "急用電話番号",
                            "FINSCHOOL_NAME"    => "出身学校"
                          );

        $header[2] = array( "HR_NAME"           => "年組",
                            "SCHREGNO"          => "生徒番号",
                            "ATTENDNO"          => "出席番号",
                            "NAME"              => "氏名",
                            "NAME_KANA"         => "よみ",
                            "ZIPCD"             => "郵便番号",
                            "ADDR1"             => "住所",
                            "TEL"               => "電話番号",
                            "GUARD_NAME"        => "保護者名",
                            "GUARD_KANA"        => "保護者よみ",
                            "GUARD_ZIPCD"       => "保護者郵便番号",
                            "GUARD_ADDR1"       => "保護者住所",
                            "GUARD_TEL"         => "保護者電話番号"
                          );

        $contents = ""; //CSVデータ出力用

        //ヘッダをセット
        $contents .= implode($header[$this->field["FORM"]], ",")."\n";

        //データをセット
        $query = knja170Query::getCsvData($this);
        $result = $db->query($query);
        $data = false;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //ヘッダ項目のみセット
            foreach ($header[$this->field["FORM"]] as $key => $val) {
                //和暦変換
                if ($key == "BIRTHDAY") $row[$key] = common::DateConv1(str_replace("-","/",$row[$key]),0);
                //印刷対象項目チェックなしのデータ
                if (($key == "TEL" || $key == "GUARD_TEL") && $this->field["TEL"] == "") $row[$key] = "";

                $contents .= $row[$key].",";
            }
            $contents .= "\n";
            $data = true;
        }

        //DB切断
        Query::dbCheckIn($db);

        if ($data){
            $couse="生徒名簿";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }
    }//getDownloadModelの綴り

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
