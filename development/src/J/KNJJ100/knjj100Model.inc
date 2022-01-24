<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjj100Model extends Model
{
    public $field      = array();
    public $cmd;
    public $control;       //コントロールマスタ
    public $staffcd;       //職員コード
    public $selectdata = array();

    /**
     * メンバ変数の初期化
     */
    public function knjj100Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    public function init()
    {
        $this->field = array("DATE"         =>  VARS::post("DATE"),         //処理年月日
                             "YEAR"         =>  VARS::post("YEAR"),         //年度
                             "GAKKI"        =>  VARS::post("GAKKI"),        //学期コード
                             "J004"         =>  VARS::post("J004"),         //対象学期
                             "DBNAME"       =>  VARS::post("DBNAME")        //DB変更に備えて
                     );

        $this->cmd          = VARS::request("cmd");
        $this->selectdata   = VARS::post("selectdata");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //タイトル行
        $wrk_nendo = common::DateConv1(str_replace("-", "/", CTRL_DATE), 10);
        $title = "委員会名簿一覧（クラス単位）";
        $wrk_date = common::DateConv1(str_replace("-", "/", CTRL_DATE), 0);

        $contents  = "{$wrk_nendo}　{$title},,,,{$wrk_date}\n";

        $data = false;
        $dataTemp = array();
        //ヘッダの作成//
        $header = array("HR_NAMEABBV"   => "年組",
                        "ATTENDNO"      => "出席番号",
                        "NAME"          => "氏名",
                        "COMMITTEE"     => "委員会名／係名",
                        "EXECUTIVENAME" => "役職"
                        );

        $contents .= implode($header, ",") ."\n";

        //出力データの作成//
        $result = $db->query(knjj100Query::selectCsvQuery($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $slash = ($row["COMMITTEENAME"] && $row["CHARGENAME"]) ? "／" : "";
            $semester = $db->getOne(knjj100Query::getNameMst('J004', $row["SEMESTER"]));

            $dataTemp["HR_NAMEABBV"]    = $row["HR_NAMEABBV"];
            $dataTemp["ATTENDNO"]       = $row["ATTENDNO"];
            $dataTemp["NAME"]           = $row["NAME"];
            $dataTemp["COMMITTEE"]      = $semester.' '.$row["COMMITTEENAME"].$slash.$row["CHARGENAME"];
            $dataTemp["EXECUTIVENAME"]  = $db->getOne(knjj100Query::getNameMst('J002', $row["EXECUTIVECD"]));

            $contents .= implode($dataTemp, ",") ."\n";
            $data = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        if ($data) {
            $couse = "{$wrk_nendo}　委員会名簿一覧（クラス単位）";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
