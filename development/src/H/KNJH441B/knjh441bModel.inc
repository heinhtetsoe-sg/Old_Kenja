<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjh441bModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjh441bModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->getPropertiesAll(); //プロパティファイル

        $this->cmd = VARS::request("cmd");

        $this->field = array("SCHOOL_KIND"  =>  VARS::post("SCHOOL_KIND")   //校種
                            , "GRADE"        =>  VARS::post("GRADE")        //学年
                        );

        //選択模試
        $this->selectTestInfo = array();
        if (VARS::post("selectTestInfo")) {
            $this->selectTestInfo = explode(",", VARS::post("selectTestInfo"));
        }
        //選択科目
        $this->selectSubclass = array();
        if (VARS::post("selectSubclass")) {
            $this->selectSubclass = explode(",", VARS::post("selectSubclass"));
        }
        //ヘッダ出力
        $this->chk_header = "";
        if (VARS::post("chk_header")) {
            $this->chk_header = explode(",", VARS::post("chk_header"));
        }

    }

    /**
     * 変数をクリアします
     */
    function clean() {
    }

    //入力項目チェック
    function validateInput() {

        // 模試情報選択
        if (get_count($this->selectTestInfo) <= 0) {
            $this->setWarning("MSG310", "模試");
            return false;
        }
        // 科目情報選択
        if (get_count($this->selectSubclass) <= 0) {
            $this->setWarning("MSG310", "科目");
            return false;
        }
        return true;
    }

    /**
     * CSV出力処理
     */
    function getDownloadModel() {

        //入力チェック
        if (!$this->validateInput()) {
            return false;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //ヘッダの作成//
        $header = array();
        $header['HR_CLASS_ATTENDNO'] = "年組番号";
        $header['SCHREGNO']          = "学籍番号";
        $header['NAME']              = "氏名";

        $contents = "";
        if ($this->chk_header) {
            $contents .= implode($header, ",");
        }

        $isData = false;
        // 科目＆模試情報の取得
        $mockTitleList = array();
        $sortList = array();
        $query = knjh441bQuery::getMockSubclass($this);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $mockSubclassCd = $row["MOCKCD"]."_".$row["MOCK_SUBCLASS_CD"];
            $mockTitle = "{$row["MOCKNAME"]}({$row["SUBCLASS_NAME"]})";
            $mockTitleList[$mockSubclassCd] = str_replace(",", ".", $mockTitle);
            $sortList[$row["MOCK_SUBCLASS_CD"]][$row["MOCKCD"]] = $row["MOCKCD"];
            $isData = true;
        }

        //科目＆模試情報がない場合は出力なし
        if (!$isData) {
            $this->setWarning("MSG303");
            return false;
        }

        $mockSubclassList = array();
        // 画面で選択された順にソート(科目毎 → 模試でソート)
        for ($i=0; $i < get_count($this->selectSubclass); $i++) { 
            $subclassCd = $this->selectSubclass[$i];
            if (isset($sortList[$subclassCd])) {
                for ($j=0; $j < get_count($this->selectTestInfo); $j++) { 
                    $mockCd = $this->selectTestInfo[$j];
                    if (isset($sortList[$subclassCd][$mockCd])) {
                        $mockSubclassList[] = $mockCd."_".$subclassCd;
                    }
                }
            }
        }

        //ソートした模試科目のタイトル出力
        for ($i=0; $i < get_count($mockSubclassList); $i++) { 
            $mockKey = $mockSubclassList[$i];
            $contents .= ",".$mockTitleList[$mockKey];
        }
        $contents .= "\n";

        // データ出力有無
        $isData = false;
        // データの取得と成形
        $contentsData = array();
        $query = knjh441bQuery::selectCsvQuery($this);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",", ".", $row);
            $row = str_replace("\r\n", "", $row);

            //年組番号
            $row["HR_CLASS_ATTENDNO"] = $row["HR_NAME"]."".$row["ATTENDNO"];

            $sep = "";
            foreach ($header as $field => $name) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }

            $schregMockSubclass = array();
            // 学生の模試情報取得
            $query = knjh441bQuery::getSchregMock($this, $row["SCHREGNO"]);
            $result2 = $db->query($query);
            while($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
                $mockSubclassCd = $row2["MOCKCD"]."_".$row2["MOCK_SUBCLASS_CD"];
                $schregMockSubclass[$mockSubclassCd] = $row2["LEVEL"];
            }
            $result2->free();
            // 科目＆模試情報出力
            for ($i=0; $i < get_count($mockSubclassList); $i++) { 
                $mockSubclassCd = $mockSubclassList[$i];
                if ($schregMockSubclass[$mockSubclassCd]) {
                    $contents .= ",".str_replace(",", ".", $schregMockSubclass[$mockSubclassCd]);
                } else {
                    $contents .= ",";
                }
            }

            $contents .= "\n";
            $isData = true;
        }
        $result->free();

        Query::dbCheckIn($db);

        if ($isData){
            $dataName = "外部模試データ";
            $fileName = sprintf("%s.csv", $dataName);
            common::downloadContents($contents, $fileName, "SJIS-win");
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }

    }

}
?>
