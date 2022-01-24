<?php

require_once('for_php7.php');

class knje380aModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $control;   //コントロールマスタ
    public $staffcd;   //登録者コード
    public $schoolCd;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    public function knje380aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //登録者コード
    }
    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->field = array("YEAR"    => VARS::post("YEAR"),   //年度一覧
                             "MAJORCD" => VARS::post("MAJORCD"),//学科一覧
                             "GRD"     => VARS::post("GRD"),    //既卒生含む
        );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");        //ファイルのアップロード
            $this->field["FILE"]["name"] = i18n_convert($this->field["FILE"]["name"], "UTF-8", "SJIS-win");
        }
    }
    public function &getMainModel()
    {
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //項目
        $header_err = array("MSGROW"    => "行番号",
                            "MSGREMARK" => "メッセージ");

        //ヘッダー項目
        $header["SCHREGNO"]        = "No.";
        $header["FINSCHOOLCD"]     = "出身";
        $header["SEX"]             = "性別";
        $header["SENKOU_KIND"]     = "進路状況";
        $header["STAT_PREF_CD"]    = "県内外別";
        $header["STAT_CD"]         = "進学先・就職";
        $header["FACULTYCD"]       = "学部";
        $header["DEPARTMENTCD"]    = "学科・コース・専攻";
        $header["SCHOOL_SORT"]     = "進学先種別";
        $header["SENKOU_NO"]       = "求人票No.（業種）";
        $header["STAT"]            = "業種";
        $header["JOBTYPE_SCD"]     = "求人票No.（職種）";
        $header["PREF"]            = "職種";
        $header["INDUSTRY"]        = "求人票に依らない場合の業種";
        $header["JOBTYPE"]         = "求人票に依らない場合の職種";
        $header["REASON1"]         = "志望した理由第一";
        $header["REASON2"]         = "志望した理由第二";
        $header["CHALLENGE_SEMI1"] = "夢実現進学チャレンジセミナー";
        $header["CHALLENGE_SEMI2"] = "学びの力向上セミナー";

        $contents  = "";//クリア

        $contents .= implode($header, ",")."\n";
        $header_data = false;
        $table = "";
        $where = "";
        $query = knje380aQuery::getselectDataSQL($this);
        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $contents .= implode($row, ",") ."\n";
            $header_data = true;
        }
        $result->free();
        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $couse    = "進路状況調査票";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
