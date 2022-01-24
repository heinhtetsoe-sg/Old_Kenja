<?php

require_once('for_php7.php');

require_once('knji070Model.inc');
require_once('knji070Query.inc');

class knji070Controller extends Controller {
    var $ModelClassName = "knji070Model";
    var $ProgramID      = "KNJI070";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":                                //メニュー画面もしくはSUBMITした場合
//                  $sessionInstance->knji070Model();       コントロールマスタの呼び出し
                    $this->callView("knji070Form1");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXSEARCH4/knjxsearch4index.php?PROGRAMID=" .$this->ProgramID 
                                        ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}&TARGET=right_frame&PATH=" .urlencode("/I/KNJI070/knji070index.php?cmd=edit");
                    $args["right_src"] = "knji070index.php?cmd=edit";
                    $args["cols"] = "58%,*";
                    View::frame($args);
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knji070Ctl = new knji070Controller;
//var_dump($_REQUEST);
?>
