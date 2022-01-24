<?php

require_once('for_php7.php');


class knjz440Model extends Model {

    var $cmd;
    var $field      = array();

    function knjz440Model() {
    }

    function init()
    {
        //プロパティファイル
        $this->getPropertiesAll();
        $this->cmd = VARS::request("cmd");

        $this->field = array("CLASSCD"          => VARS::post("CLASSCD"),
                             "CURRICULUM_CD"    => VARS::post("CURRICULUM_CD"),
                             "COURSE_MAJOR"     => VARS::post("COURSE_MAJOR"),
                             "CHECKSUBCLASS"    => VARS::post("CHECKSUBCLASS")
        );

    }
    
    //入力項目チェック
    function validateInput()
    {
        if ($this->field["CLASSCD"] == "" &&
            $this->field["CURRICULUM_CD"] == "") {
            $this->setWarning("MSG301","( 科目データが不正です )"); 
            return false;
        }

        return true;
    }

    /**
    ** 編集完了用のモデルを返します
    **/
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            //DB接続
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $query = knjz440Query::getDeleteQuery($this);
            $db->query($query);

            if (is_array($this->field["CHECKSUBCLASS"])) {
                $i = 1;
                foreach($this->field["CHECKSUBCLASS"] as $key => $val) {
                    $seqAndSubclassCd = explode(":", $val);     //【"SEQ":"SUBCLASSCD"】の形のデータが$valには格納されている
                    $seq        = $seqAndSubclassCd[0];
                    $subclassCd = $seqAndSubclassCd[1];

                    if($beforSeq != '' && $beforSeq != $seq) {  //SEQの連番をふりなおす。
                        $i++;
                    }
                    $query = knjz440Query::getUpdateQuery($i, $subclassCd, $this);
                    $db->query($query);
                    $beforSeq = $seq;
                }
            }

            $this->setMessage("MSG201");
            $db->commit();

        }
        return;
    }
}
?>
