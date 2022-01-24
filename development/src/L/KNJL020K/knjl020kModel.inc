<?php

require_once('for_php7.php');

class knjl020kModel extends Model {
    var $cmd;
    var $testdiv;
    var $acceptno;
    var $acceptno2;
    var $field = array();
    var $field2 = array();
    var $create_date;
    var $org;
    var $datadiv;
    var $year;
    
    function knjl020kModel()
    {
    }

    function init()
    {
        $this->year = CTRL_YEAR + 1;

        $this->cmd          = VARS::request("cmd");

        if (VARS::get("PROGRAMID")){
            unset($this->acceptno);
            unset($this->acceptno2);
            unset($this->testdiv);
        }
        if (VARS::post("TESTDIV")){
            //試験区分
            $this->testdiv     = VARS::post("TESTDIV");
        }
        if (is_numeric(VARS::post("ACCEPTNO"))){
            //受付NO
            $this->acceptno    = sprintf("%04d" ,(int) VARS::post("ACCEPTNO"));
        }
        //コピー元受付NO
        if (is_numeric(VARS::post("ACCEPTNO2"))){
            //受付NO
            $this->acceptno2    = sprintf("%04d" ,(int) VARS::post("ACCEPTNO2"));
        }
        //1:出身学校　2:塾
        if (VARS::get("datadiv")){
            $this->datadiv          = VARS::get("datadiv");
        }
        //事前相談ヘッダデータ
        $this->field  = array("NAME"        => VARS::post("NAME"),
                              "NAME_KANA"   => VARS::post("NAME_KANA"),
                              "SEX"         => VARS::post("SEX"),
                              "FS_CD"       => VARS::post("FS_CD"),
                              "PS_CD"       => VARS::post("PS_CD"),
                              "FS_ITEM"     => VARS::post("FS_ITEM"),
                              "PS_ITEM"     => VARS::post("PS_ITEM"),
                              "FS_UPDATED"  => VARS::post("FS_UPDATED"),
                              "PS_UPDATED"  => VARS::post("PS_UPDATED"),
                              "FS_ACCEPTNO" => VARS::post("FS_ACCEPTNO"),
                              "PS_ACCEPTNO" => VARS::post("PS_ACCEPTNO"),
                              "ACCEPTNO"    => VARS::post("ACCEPTNO2")
                              );
        
        //事前相談データ
        $this->field2 = array("F_SHDIV"     => VARS::post("F_SHDIV"),
                              "P_SHDIV"     => VARS::post("P_SHDIV"),
                              "F_JUDGEMENT" => VARS::post("F_JUDGEMENT"),
                              "P_JUDGEMENT" => VARS::post("P_JUDGEMENT"),
                              "F_COURSE"    => VARS::post("F_COURSE"),
                              "P_COURSE"    => VARS::post("P_COURSE")
                              );
    }
    // 検索用のモデルを返します
    function &getMainModel()
    {
        if ($this->cmd == "dialog"){
            $this->acceptno2 = $this->acceptno;
        }
        if ($this->cmd == "search" && !is_numeric($this->acceptno)){
            $this->setWarning("MSG301");
            return false;
        }
        if($this->cmd == "back1" || $this->cmd == "next1" || $this->cmd == "reference"){
            $db = Query::dbCheckOut();
            //志願者基礎データを取得する
            $Row = $db->getRow(knjl020kQuery::selectQuery($this), DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);
            //前次を検索してデータがなければ、メッセージ表示
            if (!is_array($Row)){
                $this->setWarning("MSG303");
            }
        }
        if ($this->cmd == "testDivChange" || VARS::get("PROGRAMID")){
            unset($this->acceptno);
            unset($this->acceptno2);
        }
    }
    //入力項目チェック
    function validateInput()
    {
        if ($this->cmd == "update" && strlen($this->create_date) == 0){
            $this->setWarning("MSG301");
            return false;
        }
        if (strlen($this->testdiv) == 0){
            $this->setWarning("MSG301","( 試験区分 )");
            return false;
        }
        if (strlen($this->acceptno) == 0){
            $this->setWarning("MSG301","( 受付№ )");
            return false;
        }
        if (strlen($this->field["NAME"]) == 0 || strlen($this->field["NAME_KANA"]) == 0){
            $this->setWarning("MSG301","( 氏名 or 氏名かな )");
            return false;
        }
        if (!$this->field["SEX"]){	//alp m-yama
            $this->setWarning("MSG301","( 性別 )");
            return false;
        }
        if (strlen($this->field["NAME"]) > 63){
            $this->setWarning("MSG914","( 氏名 は63バイトまでです。)");
            return false;
        }
        if (strlen($this->field["NAME"]) > 243){
            $this->setWarning("MSG914","( 氏名かな は243バイトまでです。 )");
            return false;
        }
        for($i = 0; $i < 5; $i++){
            if ($this->field["FS_ITEM"][$i] != "" && !is_numeric($this->field["FS_ITEM"][$i])){
                $this->setWarning("MSG907");
                return false;
            }
            if ($i == 0 && (float) $this->field["FS_ITEM"][$i] >= 10000){
                $this->setWarning("MSG901", "\\n出身学校の実力テスト得点は 10,000未満　　");
                return false;
            }
            if ($i > 0 && (float) $this->field["FS_ITEM"][$i] >= 1000){
                $this->setWarning("MSG901", "\\n出身学校の平均 or 偏差値は 1,000未満　　");
                return false;
            }
            if ($this->field["PS_ITEM"][$i] != "" &&!is_numeric($this->field["PS_ITEM"][$i])){
                $this->setWarning("MSG901");
                return false;
            }
            if ($i == 0 && (float) $this->field["PS_ITEM"][$i] >= 10000){
                $this->setWarning("MSG901", "\\n塾の実力テスト得点は 10,000未満　　");
                return false;
            }
            if ($i > 0 && (float) $this->field["PS_ITEM"][$i] >= 1000){
                $this->setWarning("MSG901", "\\n塾の平均 or 偏差値は 1,000未満　　");
                return false;
            }
        }
        if ($this->cmd == "insert"){
            $db = Query::dbCheckOut();
            //志願者基礎データを取得する
            $Row = $db->getRow(knjl020kQuery::selectQuery($this), DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);
            if (is_array($Row)){
                $this->setWarning("MSG302");
                return false;
            }
        }else if ($this->cmd == "update"){
            $db = Query::dbCheckOut();
            //志願者基礎データを取得する
            $Row = $db->getRow(knjl020kQuery::selectQuery($this), DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);
            if (!is_array($Row)){
                $this->setWarning("MSG303");
            }
        }
        return true;
    }
    // 登録用のモデルを返します
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->insert();
            $this->setMessage("MSG201");
        }
    }
    function &insert()
    {
        $result = knjl020kQuery::insert($this);
        return $result;
    }
    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            if ($this->cmd == "update"){
                $this->setMessage("MSG201");
            }else if ($this->cmd == "pre_update" || $this->cmd == "next_update"){
                $db = Query::dbCheckOut();
                //志願者基礎データを取得する
                $Row = $db->getRow(knjl020kQuery::selectQuery($this), DB_FETCHMODE_ASSOC);
                Query::dbCheckIn($db);
                if (!is_array($Row)){
                    $this->setWarning("MSG303","更新しましたが、移動先のデータが存在しません。");
                }
            }
        }
    }

    function &update()
    {
        $result = knjl020kQuery::update($this);
        return $result;
    }
    // 削除用のモデルを返します
    function &getDeleteModel()
    {
        if (strlen($this->acceptno) == 0){
            $this->setWarning("MSG301","( 受付№ )");
            return false;
        }
        $this->delete();
        $this->setMessage("MSG201");
        unset($this->acceptno);
    }
    function &delete()
    {
        $result = knjl020kQuery::delete($this);
        return $result;
    }
    // コピー用のモデルを返します
    function &getCopyModel()
    {
        if (strlen($this->acceptno2) == 0){
            $this->setWarning("MSG301","( 受付№ )");
            return false;
        }
        $tmp = $this->acceptno;
        $this->acceptno = $this->acceptno2;
        $db = Query::dbCheckOut();
        //志願者基礎データを取得する
        $Row = $db->getRow(knjl020kQuery::selectQuery($this), DB_FETCHMODE_ASSOC);
        $this->acceptno = $tmp;
        Query::dbCheckIn($db);
        if (!is_array($Row)){
            $this->setWarning("MSG303");
            return false;
        }
        $this->copy();
        $msg = $this->errorMessage("MSG201");
        echo <<<EOP
            <script language="javascript">                
                top.main_frame.closeit();
                alert('$msg');
                top.main_frame.document.forms[0].cmd.value = "main";
                top.main_frame.document.forms[0].submit();
            </script>
        
EOP;
    }
    function &copy()
    {
        $result = knjl020kQuery::copy($this);
        return $result;
    }
    // 削除用のモデルを返します
    function &getDelete2Model()
    {
        if (strlen($this->acceptno2) == 0){
            $this->setWarning("MSG301","( 受付№ )");
            return false;
        }
        $this->acceptno = $this->acceptno2;
        $db = Query::dbCheckOut();
        //志願者基礎データを取得する
        $Row = $db->getRow(knjl020kQuery::selectQuery($this), DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        if (!is_array($Row)){
            $this->acceptno = $tmp;
            $this->setWarning("MSG303");
            return false;
        }
        $this->delete2();
        $this->acceptno = $tmp;
        $msg = $this->errorMessage("MSG201");
        echo <<<EOP
            <script language="javascript">                
                top.main_frame.closeit();
                alert('$msg');
                top.main_frame.document.forms[0].cmd.value = "main";
                top.main_frame.document.forms[0].submit();
            </script>
        
EOP;
    }
    function &delete2()
    {
        $result = knjl020kQuery::delete($this);
        return $result;
    }

}
?>
