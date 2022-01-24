<?php

require_once('for_php7.php');

require_once('knji050Model.inc');
require_once('knji050Query.inc');

class knji050Controller extends Controller
{
    public $ModelClassName = "knji050Model";
    public $ProgramID      = "KNJI050";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":                                //メニュー画面もしくはSUBMITした場合
//                  $sessionInstance->knji050Model();       コントロールマスタの呼び出し
                    $this->callView("knji050Form1");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXSEARCH4/knjxsearch4index.php?PROGRAMID=" .$this->ProgramID
                                        ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}"
                                        ."&TARGET=right_frame&PATH=" .urlencode("/I/KNJI050/knji050index.php?cmd=edit");
                    $args["right_src"] = "knji050index.php?cmd=edit";
                    $args["cols"] = "50%,*";
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
$knji050Ctl = new knji050Controller();
//var_dump($_REQUEST);
