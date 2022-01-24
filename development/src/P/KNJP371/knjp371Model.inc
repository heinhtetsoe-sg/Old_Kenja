<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjp371Model extends Model {
    var $field       = array();
    var $select_opt  = array();
    var $cmd;
    var $selectleft;
    var $staffcd;
    var $date;

    /**
     * メンバ変数の初期化
     */
    function knjp371Model()
    {
    }

    function init()
    {
        $this->field = array("GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "YEAR_MONTH"       =>  VARS::post("YEAR_MONTH"),       //対象年月
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd        = VARS::request("cmd");
        $this->selectleft = VARS::post("selectleft");
        if ($this->cmd != 'change_class' && $this->cmd != 'read' && $this->cmd != 'update') {
            $this->select_opt = array();
        }

        if (VARS::post("STAFF")) {
            $this->staffcd  = VARS::post("STAFF");
        }
        if (VARS::post("DATE")) {
            $this->date  = VARS::post("DATE");
        }

        if ($this->cmd == "") {
            unset($this->staffcd);
        }

    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        $this->delete();
        $this->update("");
    }

    function &delete()
    {
        $result = knjp371Query::getDeleteQuery2($this);

        return $result;
    }

    function &update($cnt)
    {
        $result = knjp371Query::getUpdateQuery2($this);

        return $result;
    }

    //ＣＳＶ出力
    function getDownloadModel() {

        //DB接続
        $db = Query::dbCheckOut();

        //クリア
        $contents  = "";
        $data = false;
        $dataTemp = array();

        //ヘッダの作成//
        $header = array("TITLE"             => "タイトル",
                        "BANKNAME_KANA"     => "銀行",
                        "BRANCHNAME_KANA"   => "支店",
                        "ABBV1"             => "種別",
                        "ACCOUNTNO"         => "口座番号",
                        "ACCOUNTNAME"       => "受取人",
                        "MONEY"             => "返金額",
                        "SCHREGNO"          => "学籍番号",
                        "GRADE"             => "学年",
                        "HR_CLASS"          => "組",
                        "ATTENDNO"          => "番号",
                        "NAME"              => "生徒名"
                        );

        $contents .= implode($header, ",") ."\n";

        $var1 = $db->getOne(knjp371Query::getVar1());
        //出力データ「振込依頼書(連記式)」
        $title = "振込依頼書(連記式)";
        $isMinou = false;
        $result = $db->query(knjp371Query::meisaiSql($this, $isMinou, $var1));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row["TITLE"] = $title;
            foreach ($header as $key => $val) {
                $dataTemp[$key] = $row[$key];
            }
            $contents .= implode($dataTemp, ",") ."\n";
            $data = true;
        }
        $result->free();
        //出力データ「授業料未納者」
        $dataTemp = array();
        $title = "授業料未納者";
        $isMinou = true;
        $result = $db->query(knjp371Query::meisaiSql($this, $isMinou, $var1));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row["TITLE"] = $title;
            foreach ($header as $key => $val) {
                $dataTemp[$key] = $row[$key];
            }
            $contents .= implode($dataTemp, ",") ."\n";
            $data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        if ($data){
            $couse = "振込依頼書";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

}
?>
