<?php
//定数定義

class knjl3020Model extends Model {
    var $cmd;
    var $year;  //年度
    var $selectdata;
    var $temp_year;

    //メンバ変数の初期化
    function knjl3020Model()
    {
    }
    function init()
    {
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->applicantdiv      = VARS::post("APPLICANTDIV");
        $this->testdiv           = VARS::request("TESTDIV");

        $temp = VARS::post("examyear");
        if (isset($temp)) {
            $this->examyear      = VARS::post("examyear");
        }else{
            $this->examyear      = CTRL_YEAR + 1;           //現在処理年度を初期値にする
        }
        $this->selectdata        = VARS::post("selectdata");
        $this->rightMoveData     = VARS::post("rightMoveData");
        $this->cmd               = VARS::request("cmd");        
    }

    //入力項目チェック
    function validateInput()
    {
        if (!is_numeric($this->examyear)){
            $this->warning = "対象年度を選択してください。";
            return false;
        }

        $db = Query::dbCheckOut();

        if ($this->cmd == "update" && strlen($this->selectdata) > 0) {
            //対象年度の名称マスタに科目の設定がされていない場合はエラー
            $testSubCdArray = explode (",", $this->selectdata);
            $data009 = array();

            $query = knjl3020Query::getNameMst($this->examyear, "L009");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $data009[] = $row["VALUE"];
            }
            foreach ($testSubCdArray as $testSubCd) {
                if (!in_array($testSubCd, $data009)) {
                    $this->setWarning("MSG203", "対象年度で名称マスタ(L009)の科目設定がされていません。");
                    return false;
                }
            }            
        }

        Query::dbCheckIn($db);
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
        }
        $this->setMessage("MSG201");
        $this->examyear = "";

        return true;
    }
    //アップデート時の内部メソッド
    function &update()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //全削除
        $query = knjl3020Query::getDeleteAllQuery($this);
        $db->query($query);

        //科目コード配列に格納
        $array = explode(",", $this->selectdata);
        foreach ($array as $testSubCd) {

            $query = knjl3020Query::getUpdateRowQuery($this, $testSubCd);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $true;
    }
}
?>
