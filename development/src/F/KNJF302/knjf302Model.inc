<?php

require_once('for_php7.php');

    //定数定義
//配列内の数値のみ取得
function num_filter($var) {
    return (is_numeric($var));
}

class knjf302Model extends Model {
    var $cmd;
    var $gc_select;                     //セレクトメニュー
    var $control_data = array();        //コントロールのデータを保持
    var $staffcd;                       //セキュリティーチェック
    var $sec_competence;                //権限保持
    var $updated = array();             //UPDATED値保持
    var $fields = array();
    var $hyoteiYomikae; // 評定1の場合は2で処理する
    var $schoolcd;  //学校コード
    var $execute_date; //県への報告用登録日付
    var $docNumber;     //文書番号

    function knjf302Model() {
        //学籍処理日取得
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init() {
        $this->cmd              = VARS::request("cmd");
        $this->gc_select        = VARS::post("gc_select");        //プルダウンメニュー
        $this->hyoteiYomikae    = VARS::post("hyoteiYomikae");
        $this->execute_date     = VARS::post("EXECUTE_DATE");
        $this->docNumber        = VARS::post("DOC_NUMBER");
        $this->fixedDate        = VARS::post("FIXED_DATE");
        $this->fixedData        = VARS::post("FIXED_DATA");

        //セキュリティーチェック
        $this->sec_competence = common::SecurityCheck($this->staffcd,"KNJF302");

        $this->field = array("YEAR"             => VARS::post("YEAR"),
                             "GRADE"            => VARS::post("GRADE"),
                             "ADDITION_DATE"    => VARS::post("ADDITION_DATE"),
                             "EXECUTE_DATE"     => VARS::post("EXECUTE_DATE"),
                             "REPORT"           => VARS::post("REPORT")
                            );

        $temp = array("ABSENCE01"           => VARS::post("ABSENCE01"),
                      "ABSENCE02"           => VARS::post("ABSENCE02"),
                      "ABSENCE03"           => VARS::post("ABSENCE03"),
                      "ABSENCE04"           => VARS::post("ABSENCE04"),
                      "ABSENCE05"           => VARS::post("ABSENCE05"),
                      "ABSENCE06"           => VARS::post("ABSENCE06"),
                      "ABSENCE07"           => VARS::post("ABSENCE07"),
                      "ABSENCE08"           => VARS::post("ABSENCE08"),
                      "ATTENDSUSPEND01"     => VARS::post("ATTENDSUSPEND01"),
                      "ATTENDSUSPEND02"     => VARS::post("ATTENDSUSPEND02"),
                      "ATTENDSUSPEND03"     => VARS::post("ATTENDSUSPEND03"),
                      "ATTENDSUSPEND04"     => VARS::post("ATTENDSUSPEND04"),
                      "ATTENDSUSPEND05"     => VARS::post("ATTENDSUSPEND05"),
                      "ATTENDSUSPEND06"     => VARS::post("ATTENDSUSPEND06"),
                      "ATTENDSUSPEND07"     => VARS::post("ATTENDSUSPEND07"),
                      "ATTENDSUSPEND08"     => VARS::post("ATTENDSUSPEND08"),
                      "ATTENDSUSPEND09"     => VARS::post("ATTENDSUSPEND09"),
                      "ATTENDSUSPEND10"     => VARS::post("ATTENDSUSPEND10"),
                      "ATTENDSUSPEND11"     => VARS::post("ATTENDSUSPEND11"),
                      "TOTALSUM01"          => VARS::post("TOTALSUM01"),
                      "TOTALSUM02"          => VARS::post("TOTALSUM02"),
                      "TOTALSUM03"          => VARS::post("TOTALSUM03"),
                      "TOTALSUM04"          => VARS::post("TOTALSUM04"),
                      "TOTALSUM04_PERCENT"  => VARS::post("TOTALSUM04_PERCENT"),
                      "TOTALSUM05"          => VARS::post("TOTALSUM05"),
                      "TOTALSUM05_PERCENT"  => VARS::post("TOTALSUM05_PERCENT"),
                      "TOTALSUM06"          => VARS::post("TOTALSUM06")
                       );

        $this->fields = array_merge($this->fields, $temp);

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        if ($this->cmd == "" && $this->field["YEAR"] && $this->field["ADDITION_DATE"]) {
            $this->key_Move_Model();
        }
    }

    //キー値を移動
    function &key_Move_Model()
    {
        if (strlen($this->field["YEAR"]) > 0 && strlen($this->field["ADDITION_DATE"]) > 0) {
            if ($this->cmd == "read_before") {
                $setDate = date("Y/m/d", strtotime("{$this->field["ADDITION_DATE"]} -1 day"));
            } else if ($this->cmd == "read_next") {
                $setDate = date("Y/m/d", strtotime("{$this->field["ADDITION_DATE"]} +1 day"));
            } else {
                $setDate = $this->field["ADDITION_DATE"];
            }
            //年度内の処理に限る
            if ($this->field["YEAR"]."/04/01" <= $setDate &&
                $setDate < ((int)$this->field["YEAR"] + 1)."/04/01")
            {
                $this->field["ADDITION_DATE"] = $setDate;
                return true;
            } else {
                $this->setWarning("MSG915","(処理年度範囲外)");
                return false;
            }
        }

        return;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        $this->update();
        $this->setMessage("MSG201");
    }

    //アップデート時の内部メソッド
    function &update() {
        knjf302Query::getUpdateQuery($this);
        return;
    }

    //確定
    function &getFixedUpdateModel() {
        $this->fixedUpdate();
        $this->setMessage("MSG201");
    }

    //アップデート
    function &fixedUpdate() {
        knjf302Query::getFixedUpdateQuery($this);
        return;
    }

    //県への報告
    function &getUpdateEdboardModel() {
        //県側・感染症発生状況入力管理画面テーブル
        //学校側・報告テーブル
        knjf302Query::getUpdateReport($this);
        $this->setMessage("MSG201");
    }

    //ＣＳＶ出力
    function getDownloadModel()
    {
        $header = array();
        $header[] = "";
        $header[] = "欠席者の症状(人)";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "出席停止(人)";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";

        $header2 = array();
        $header2[] = "年組";
        $header2[] = "発熱";
        $header2[] = "頭痛";
        $header2[] = "急性呼吸器症状";
        $header2[] = "下痢・腹痛";
        $header2[] = "嘔吐・嘔気";
        $header2[] = "発疹";
        $header2[] = "インフルエンザ様症状";
        $header2[] = "その他";
        $header2[] = "インフルエンザ";
        $header2[] = "感染性胃腸炎";
        $header2[] = "溶連菌感染症";
        $header2[] = "おたふくかぜ";
        $header2[] = "水ぼうそう";
        $header2[] = "マイコプラズマ感染症";
        $header2[] = "伝染性紅斑";
        $header2[] = "手足口病";
        $header2[] = "咽頭結膜熱";
        $header2[] = "流行性角結膜炎";
        $header2[] = "その他";
        $header2[] = "罹患出席者数";
        $header2[] = "欠席者数";
        $header2[] = "出席停止者数";
        $header2[] = "総患者数(人数)";
        $header2[] = "総患者数(割合)";
        $header2[] = "総欠席者数(人数)";
        $header2[] = "総欠席者数(割合)";
        $header2[] = "在籍者数";

        $contents  = "";    //クリア

        $contents .= implode($header, ",")."\n";
        $contents .= implode($header2, ",")."\n";

        $header_data = false;
        //DB接続
        $db = Query::dbCheckOut();
        $table = "";
        $where = "";
        $query = knjf302Query::selectCsvQuery($this);
        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            $contents .= implode($row, ",") ."\n";
            $header_data = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $couse = "感染症発生状況入力";
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>
